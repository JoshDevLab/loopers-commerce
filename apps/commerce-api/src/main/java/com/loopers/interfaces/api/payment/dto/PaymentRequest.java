package com.loopers.interfaces.api.payment.dto;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;

public record PaymentRequest(
        Long orderId,
        String paymentType,
        String cardType,
        String cardNo,
        String callbackUrl
) {
    public PaymentCommand.Request toCommand() {
        return new PaymentCommand.Request(
                orderId,
                Payment.PaymentType.valueOfName(paymentType),
                CardType.valueOfName(cardType),
                CardNo.valueOfName(cardNo),
                callbackUrl
        );
    }
}
