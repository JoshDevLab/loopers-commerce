package com.loopers.domain.payment;

public abstract class ExternalPaymentResponse {
    public abstract String getTransactionId();
    public abstract boolean checkSync(PaymentCommand.CallbackRequest command);
}
