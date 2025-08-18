package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.ExternalPaymentRequest;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentProcessor;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoopersPgProcessor implements PaymentProcessor {
    private final LoopersPgFeginClient client;

    @Override
    public Payment.PaymentType getPaymentType() {
        return Payment.PaymentType.CARD;
    }

    @Override
    public ExternalPaymentResponse payment(ExternalPaymentRequest paymentRequest) {
        try {
            if (!(paymentRequest instanceof LoopersPgFeginClient.LoopersPaymentRequest loopersPaymentRequest)) {
                throw new CoreException(ErrorType.INVALID_PAYMENT_REQUEST_TYPE, "결제 벤더사에 맞지 않는 요청타입니다.");
            }
            // 카드사 PG 호출 로직
            return client.processPayment(loopersPaymentRequest);
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExternalPaymentRequest createRequest(PaymentCommand.Request paymentCommand, BigDecimal paidAmount) {
        return new LoopersPgFeginClient.LoopersPaymentRequest(
                paymentCommand.orderId(),
                paymentCommand.cardType(),
                paymentCommand.cardNo(),
                paymentCommand.callbackUrl(),
                paidAmount
        );
    }
}
