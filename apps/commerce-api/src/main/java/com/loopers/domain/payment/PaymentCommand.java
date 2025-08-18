package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.dto.CardNo;

public class PaymentCommand {
    public record Request(
            Long orderId,
            Payment.PaymentType paymentType,
            CardType cardType,
            CardNo cardNo,
            String callbackUrl
    ) {}
}

