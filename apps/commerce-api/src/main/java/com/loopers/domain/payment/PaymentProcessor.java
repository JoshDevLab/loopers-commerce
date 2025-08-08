package com.loopers.domain.payment;

import java.math.BigDecimal;

public interface PaymentProcessor {
    Payment.PaymentType getPaymentType();
    boolean payment(BigDecimal amount);
}
