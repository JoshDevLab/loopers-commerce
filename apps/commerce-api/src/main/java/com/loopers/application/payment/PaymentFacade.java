package com.loopers.application.payment;

import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.ExternalPaymentResponse;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
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
    private final NotificationService notificationService;
    private final PaymentExceptionTranslator exceptionTranslator;

    public PaymentInfo payment(PaymentCommand.Request paymentCommand) {
        Order order = orderService.findByIdWithLock(paymentCommand.orderId());

        if (paymentService.existsByOrderIdAndStatus(order.getId(), Payment.PaymentStatus.SUCCESS)) {
            throw new CoreException(ErrorType.ALREADY_EXIST_ORDER_PAYMENT, order.getId() + " 는 이미 결제가 완료된 주문입니다.");
        }

        Payment payment = paymentService.create(paymentCommand, order.getPaidAmount());
        ExternalPaymentResponse response = exceptionTranslator.execute(() -> paymentService.payment(payment));

        Payment updatedPayment = paymentService.updateTransactionId(payment.getId(), response.getTransactionId());
        return PaymentInfo.of(updatedPayment);
    }

    @Retry(name = "payment-callback-sync", fallbackMethod = "fallbackProcessCallback")
    public PaymentInfo processCallback(PaymentCommand.CallbackRequest command) {
        ExternalPaymentResponse response = exceptionTranslator.executeForCallback(
            () -> paymentService.getTransactionIdFromExternal(command.transactionKey())
        );
        
        boolean isSync = response.checkSync(command);
        
        if (!isSync) {
            throw new CoreException(ErrorType.CALLBACK_DATA_SYNC_FAILED, "콜백 데이터 동기화 실패");
        }

        if (response.isSuccess()) {
            return PaymentInfo.of(paymentService.complete(command));
        }

        return PaymentInfo.of(paymentService.failed(command));
    }

    // Fallback 메서드
    private PaymentInfo fallbackProcessCallback(PaymentCommand.CallbackRequest command, Exception ex) {
        log.error("콜백 데이터 동기화 최종 실패 - transactionKey: {}", command.transactionKey(), ex);
        
        // 알림 발송
        notificationService.sendPaymentSyncFailureAlert(command.transactionKey());
        
        throw new CoreException(ErrorType.PAYMENT_FAIL, "콜백 데이터 동기화 최종 실패 - transactionKey: " + command.transactionKey());
    }
}
