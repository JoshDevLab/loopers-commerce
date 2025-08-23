package com.loopers.domain.payment;

import java.math.BigDecimal;

public abstract class ExternalPaymentResponse {
    public abstract String getTransactionId();
    public abstract boolean checkSync(PaymentCommand.CallbackRequest command);
    public abstract boolean isSuccess();
    public abstract String getStatus();
    public abstract String getReason();
    
    // 추가 메서드들 (선택적 구현)
    public BigDecimal getAmount() { return null; }
}

