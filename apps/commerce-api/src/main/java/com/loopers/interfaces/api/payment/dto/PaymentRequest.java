package com.loopers.interfaces.api.payment.dto;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;

public record PaymentRequest(
        Long orderId,
        String paymentType,
        PaymentCommand.PaymentData paymentData
) {
    public PaymentCommand.Request toCommand() {
        return new PaymentCommand.Request(orderId, Payment.PaymentType.valueOfName(paymentType), paymentData);
    }
}
