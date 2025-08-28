package com.loopers.domain.product.like;

import com.loopers.domain.product.ProductChangedEvent;

public interface ProductChangedEventPublisher {
    void publish(ProductChangedEvent event);
}
