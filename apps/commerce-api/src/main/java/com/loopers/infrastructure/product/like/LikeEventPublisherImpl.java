package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.like.ProductLikeEvent;
import com.loopers.domain.product.like.ProductLikeEventPublisher;
import com.loopers.domain.product.like.ProductUnLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeEventPublisherImpl implements ProductLikeEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductLikeEvent event) {
        log.info("Publishing event for product_like start {}", event.productId());
        applicationEventPublisher.publishEvent(event);
        log.info("Publishing event for product_like success {}", event.productId());
    }

    @Override
    public void publish(ProductUnLikeEvent event) {
        log.info("Publishing event for product_unlike start {}", event.productId());
        applicationEventPublisher.publishEvent(event);
        log.info("Publishing event for product_unlike success {}", event.productId());
    }
}
