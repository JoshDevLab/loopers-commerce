package com.loopers.application.payment;

import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.payment.*;
import com.loopers.scheduling.annotation.LoopersScheduled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentSyncScheduler {

    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OrderPaymentProcessor orderPaymentProcessor;
    private final PaymentEventPublisher paymentEventPublisher;

    /**
     * 결제 상태 동기화 작업
     * 매분 실행하여 PENDING 상태의 결제 건들을 PG사와 동기화
     */
    @LoopersScheduled(
        name = "sync-pending-payments",
        description = "PENDING 상태 결제 건들의 PG 상태 동기화",
        cron = "0 * * * * *",  // 매분 실행
        profiles = {"local", "dev", "qa", "prd"}
    )
    public void syncPendingPayments() {
        try {
            List<Payment> pendingPayments = paymentService.findPendingPayments();
            
            if (pendingPayments.isEmpty()) {
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            int notificationCount = 0;
            
            for (Payment payment : pendingPayments) {
                try {
                    // 10분 이상 PENDING 상태인 경우 알림 처리하고, 원복
                    if (isOverTenMinutes(payment.getCreatedAt())) {
                        paymentEventPublisher.publish(PaymentEvent.PaymentFailedRecovery.of(payment.getOrder().getId()));
                        notificationService.sendPaymentSyncFailureAlert(payment.getTransactionId());
                        notificationCount++;
                    }

                    boolean syncResult = paymentService.hasSyncPaymentStatus(payment);
                    
                    if (syncResult) {
                        successCount++;
                        orderPaymentProcessor.completeOrderAndPayment(PaymentCommand.CallbackRequest.create(payment.getTransactionId(),
                                payment.getOrder().getId().toString(),
                                payment.getCardType().name(),
                                payment.getCardNo().getValue(),
                                payment.getPaidAmount(),
                                payment.getStatus().toString(),
                                "결제성공"));
                    } else {
                        failCount++;
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("결제 상태 동기화 중 오류 발생: paymentId={}", payment.getId(), e);
                }
            }
            
            log.info("결제 상태 동기화 완료 - 성공: {}, 실패: {}, 알림: {}", 
                successCount, failCount, notificationCount);
            
        } catch (Exception e) {
            log.error("결제 상태 동기화 작업 실패", e);
        }
    }

    /**
     * 결제 생성 시점이 10분 이상 지났는지 확인
     */
    private boolean isOverTenMinutes(ZonedDateTime createdAt) {
        return createdAt.isBefore(ZonedDateTime.now().minusMinutes(10));
    }

}
