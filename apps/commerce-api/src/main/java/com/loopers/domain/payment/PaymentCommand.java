package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.dto.CardNo;

import java.math.BigDecimal;

public class PaymentCommand {
    public record Request(
            Long orderId,
            Payment.PaymentType paymentType,
            CardType cardType,
            CardNo cardNo,
            String callbackUrl
    ) {}

    public record CallbackRequest(
            String transactionKey,
            String orderId,
            String cardType,
            String cardNo,
            BigDecimal amount,
            String status,
            String reason
    ) {
        public static CallbackRequest create(String transactionId,
                                             String id,
                                             String cardType,
                                             String cardNo,
                                             BigDecimal paidAmount,
                                             String status,
                                             String reason) {
            return new CallbackRequest(
                   transactionId,
                   id,
                   cardType,
                   cardNo,
                   paidAmount,
                   status,
                   reason
            );
        }

        public boolean isSuccess() {
            return status.equals("SUCCESS");
        }
    }
}

