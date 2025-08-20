package com.loopers.domain.payment;

import lombok.Getter;

/**
 * 결제 관련 이벤트
 */
public class PaymentEvent {

    /**
         * 결제 실패로 인한 복구 이벤트
         */
        @Getter
        public record PaymentFailedRecovery(Long orderId) {
            public static PaymentFailedRecovery of(Long orderId) {
                return new PaymentFailedRecovery(orderId);
            }
        }
}
