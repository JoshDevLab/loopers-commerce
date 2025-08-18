package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class LoopersPgProcessor implements PaymentProcessor {
    private final LoopersPgClient loopersPgClient;

    @Override
    public Payment.PaymentType getPaymentType() {
        return Payment.PaymentType.CARD;
    }

    @Override
    public boolean payment(BigDecimal amount) {
        return false;
    }
}
