package com.loopers.domain.payment;

import java.math.BigDecimal;

public interface PaymentProcessor {
    Payment.PaymentType getPaymentType();
    ExternalPaymentResponse payment(ExternalPaymentRequest paymentRequest);
    ExternalPaymentRequest createRequest(PaymentCommand.Request paymentCommand, BigDecimal paidAmount);
    ExternalPaymentResponse getByTransactionKey(String transactionId);
}
