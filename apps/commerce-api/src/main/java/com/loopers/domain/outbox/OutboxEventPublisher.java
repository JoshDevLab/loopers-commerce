package com.loopers.domain.outbox;

import com.loopers.application.product.ProductViewEvent;
import com.loopers.domain.order.OrderCreatedEvent;
import com.loopers.domain.product.like.ProductLikeEvent;
import com.loopers.domain.product.like.ProductUnLikeEvent;

public interface OutboxEventPublisher {
    void publish(OrderCreatedEvent event);
    void publish(ProductLikeEvent event);
    void publish(ProductUnLikeEvent event);
    void publish(ProductViewEvent productViewEvent);
}
