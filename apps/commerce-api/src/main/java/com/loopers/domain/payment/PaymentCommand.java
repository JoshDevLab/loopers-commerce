package com.loopers.domain.payment;

public class PaymentCommand {
    public record Request(
            Long orderId,
            Payment.PaymentType paymentType,
            PaymentData paymentData
    ) {}

    public record PaymentData(
            String successUrl,
            String failUrl,
            String cancelUrl
    ) {}
}

