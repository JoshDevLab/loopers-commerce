package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.interfaces.api.payment.dto.CardNo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {
    private Long id;
    private Long orderId;
    private CardType cardType;
    private CardNo cardNo;
    private Payment.PaymentType type;
    private Payment.PaymentStatus status;
    private BigDecimal paidAmount;
    private String transactionId;

    public static PaymentInfo of(Payment payment) {
        return new PaymentInfo(payment.getId(),
                payment.getOrder().getId(),
                payment.getCardType(),
                payment.getCardNo(),
                payment.getPaymentType(),
                payment.getStatus(),
                payment.getPaidAmount(),
                payment.getTransactionId()
        );
    }
}
