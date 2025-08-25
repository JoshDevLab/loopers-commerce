package com.loopers.domain.payment;

import java.math.BigDecimal;

public interface PaymentProcessor {
    Payment.PaymentType getPaymentType();
    ExternalPaymentResponse payment(ExternalPaymentRequest paymentRequest);
    ExternalPaymentRequest createRequest(Payment payment);
    ExternalPaymentResponse getByTransactionKey(String transactionId);
}
