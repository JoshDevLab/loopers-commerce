package com.loopers.domain.payment;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public abstract class ExternalPaymentRequest {
    protected BigDecimal amount;

    public ExternalPaymentRequest(BigDecimal amount) {
        this.amount = amount;
    }
}
