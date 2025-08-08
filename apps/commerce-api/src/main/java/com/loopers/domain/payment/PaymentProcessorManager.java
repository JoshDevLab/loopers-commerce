package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PaymentProcessorManager {

    private final List<PaymentProcessor> processors;
    private Map<Payment.PaymentType, PaymentProcessor> paymentProcessorMap;

    @PostConstruct
    public void init() {
        this.paymentProcessorMap = Collections.unmodifiableMap(
                processors.stream()
                        .collect(Collectors.toMap(
                                PaymentProcessor::getPaymentType,
                                Function.identity()
                        ))
        );
    }

    public PaymentProcessor getProcessor(Payment.PaymentType paymentType) {
        PaymentProcessor processor = paymentProcessorMap.get(paymentType);
        if (processor == null) {
            throw new CoreException(ErrorType.UNSUPPORTED_PAYMENT_TYPE, "지원하지 않는 결제 수단입니다: " + paymentType);
        }
        return processor;
    }
}

