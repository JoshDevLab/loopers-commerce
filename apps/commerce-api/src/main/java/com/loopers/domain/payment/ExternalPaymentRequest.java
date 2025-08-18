package com.loopers.domain.payment;

import java.math.BigDecimal;

public class ExternalPaymentRequest {
    BigDecimal amount;

    public ExternalPaymentRequest(BigDecimal amount) {
        this.amount = amount;
    }
}
