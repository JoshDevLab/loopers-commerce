package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
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
    private Payment.PaymentType type;
    private Payment.PaymentStatus status;
    private BigDecimal paidAmount;

    public static PaymentInfo of(Payment payment) {
        return new PaymentInfo(payment.getId(), payment.getOrder().getId(), payment.getType(), payment.getStatus(), payment.getPaidAmount());
    }
}
