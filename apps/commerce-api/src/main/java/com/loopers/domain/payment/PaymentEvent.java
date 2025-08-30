package com.loopers.domain.payment;

/**
 * 결제 관련 이벤트
 */
public class PaymentEvent {
    public record PaymentFailedRecovery(Long orderId) {
        public static PaymentFailedRecovery of(Long orderId) {
            return new PaymentFailedRecovery(orderId);
        }
    }

    public record PaymentSuccess(
            Long orderId
    ) {
    }
}
