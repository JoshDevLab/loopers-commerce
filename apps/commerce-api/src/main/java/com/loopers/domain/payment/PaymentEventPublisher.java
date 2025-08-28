package com.loopers.domain.payment;

/**
 * 결제 이벤트 발행자
 */
public interface PaymentEventPublisher {
    void publish(PaymentEvent.PaymentFailedRecovery event);
    void publish(PaymentEvent.PaymentSuccess event);
}
