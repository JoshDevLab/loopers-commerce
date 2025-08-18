package com.loopers.domain.payment;

import com.loopers.infrastructure.payment.ExternalPaymentResponse;

import java.math.BigDecimal;

public interface PaymentProcessor {
    Payment.PaymentType getPaymentType();
    ExternalPaymentResponse payment(ExternalPaymentRequest paymentRequest);
    ExternalPaymentRequest createRequest(PaymentCommand.Request paymentCommand, BigDecimal paidAmount);
}
