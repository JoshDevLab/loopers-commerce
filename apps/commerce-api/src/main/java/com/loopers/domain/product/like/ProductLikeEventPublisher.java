package com.loopers.domain.product.like;

public interface ProductLikeEventPublisher {
    void publish(ProductLikeEvent event);
    void publish(ProductUnLikeEvent productUnLikeEvent);
}
