package com.loopers.domain.outbox;

import com.loopers.domain.product.like.ProductLikeEvent;
import com.loopers.domain.product.like.ProductUnLikeEvent;

public interface OutboxEventPublisher {
    void publish(OutboxEvent event);
    void publish(ProductLikeEvent event);
    void publish(ProductUnLikeEvent event);
}
