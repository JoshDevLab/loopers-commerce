package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OrderPaymentProcessor orderPaymentProcessor;
    private final PaymentEventPublisher paymentEventPublisher;
    private final NotificationService notificationService;

    public PaymentInfo payment(PaymentCommand.Request paymentCommand) {
        ExternalPaymentResponse response;
        Order order = orderService.findByIdForUpdate(paymentCommand.orderId());

        if (paymentService.existsByOrderIdAndStatus(order.getId(), Payment.PaymentStatus.SUCCESS)) {
            throw new CoreException(ErrorType.ALREADY_EXIST_ORDER_PAYMENT, order.getId() + " 는 이미 결제가 완료된 주문입니다.");
        }

        Payment payment = paymentService.create(paymentCommand);

        try {
            response = paymentService.payment(paymentCommand);
        } catch (CoreException e) {
            log.error("외부 PG 결제 실패", e);
            paymentEventPublisher.publish(PaymentEvent.PaymentFailedRecovery.of(order.getId()));
            throw new CoreException(ErrorType.PAYMENT_FAIL, "외부 결제 실패로 복구 처리함");
        }

        paymentService.updateTransactionId(payment.getId(), response.getTransactionId());
        return PaymentInfo.of(payment);
    }

    @Retry(name = "payment-callback-sync", fallbackMethod = "fallbackProcessCallback")
    public PaymentInfo processCallback(PaymentCommand.CallbackRequest command) {
        log.info("콜백 데이터 동기화 시도 - transactionKey: {}", command.transactionKey());
        
        ExternalPaymentResponse response = paymentService.getTransactionIdFromExternal(command.transactionKey());
        boolean isSync = response.checkSync(command);
        
        if (!isSync) {
            log.warn("콜백 데이터 불일치 감지 - transactionKey: {}", command.transactionKey());
            throw new DataSyncException("콜백 데이터 동기화 실패");
        }
        
        log.info("콜백 데이터 동기화 성공 - transactionKey: {}", command.transactionKey());
        
        if (command.isSuccess()) {
            return PaymentInfo.of(orderPaymentProcessor.completeOrderAndPayment(command));
        }
        
        paymentEventPublisher.publish(PaymentEvent.PaymentFailedRecovery.of(PgOrderIdGenerator.extractOrderId(command.orderId())));
        return PaymentInfo.of(orderPaymentProcessor.failedOrderAndPayment(command));
    }

    // Fallback 메서드
    private PaymentInfo fallbackProcessCallback(PaymentCommand.CallbackRequest command, Exception ex) {
        log.error("콜백 데이터 동기화 최종 실패 - transactionKey: {}", command.transactionKey(), ex);
        
        // 알림 발송
        notificationService.sendPaymentSyncFailureAlert(command);
        
        throw new PaymentProcessingException("콜백 데이터 동기화 최종 실패 - 수동 확인 필요", ex);
    }
}
