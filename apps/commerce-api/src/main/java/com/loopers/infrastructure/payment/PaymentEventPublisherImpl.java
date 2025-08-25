package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.payment.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트 발행 구현체
 */
@RequiredArgsConstructor
@Component
public class PaymentEventPublisherImpl implements PaymentEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentEvent.PaymentFailedRecovery event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(PaymentEvent.PaymentSuccess event) {
        applicationEventPublisher.publishEvent(event);
    }
}
