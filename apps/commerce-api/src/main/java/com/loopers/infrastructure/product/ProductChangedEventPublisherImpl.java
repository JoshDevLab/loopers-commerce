package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.product.like.ProductChangedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductChangedEventPublisherImpl implements ProductChangedEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductChangedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
