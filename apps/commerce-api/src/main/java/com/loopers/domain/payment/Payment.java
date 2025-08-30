package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.interfaces.api.payment.dto.CardNo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment")
public class Payment extends BaseEntity {
    private String transactionId;

    private String callbackUrl;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Embedded
    private CardNo cardNo;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal paidAmount;

    private Long orderId;

    public static Payment create(PaymentType paymentType,
                                 CardType cardType,
                                 CardNo cardNo,
                                 PaymentStatus paymentStatus,
                                 BigDecimal paidAmount,
                                 Long orderId,
                                 String callbackUrl)
    {
        Payment payment = new Payment();
        payment.paymentType = paymentType;
        payment.cardType = cardType;
        payment.cardNo = cardNo;
        payment.status = paymentStatus;
        payment.paidAmount = paidAmount;
        payment.orderId = orderId;
        payment.callbackUrl = callbackUrl;
        return payment;
    }

    public void success() {
        this.status = PaymentStatus.SUCCESS;
    }

    public void failed()  {
        this.status = PaymentStatus.FAILED;
    }

    public void updateTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public enum PaymentType {
        CARD,
        BANK_TRANSFER,      // 무통장입금 (계좌이체)
        NAVER_PAY,          // 네이버페이
        KAKAO_PAY;           // 카카오페이

        public static PaymentType valueOfName(String name) {
            String upperCaseName = name.toUpperCase();
            try {
                return PaymentType.valueOf(upperCaseName);
            } catch (IllegalArgumentException e) {
                throw new CoreException(ErrorType.UNSUPPORTED_PAYMENT_TYPE, "지원하지 않는 PAYMENT_TYPE : " + name);
            }
        }
    }
    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        CANCELED
    }
}
