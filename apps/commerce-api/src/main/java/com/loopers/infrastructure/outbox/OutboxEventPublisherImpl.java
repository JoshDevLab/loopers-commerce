package com.loopers.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventPublisher;
import com.loopers.domain.product.like.ProductLikeEvent;
import com.loopers.domain.product.like.ProductUnLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventPublisherImpl implements OutboxEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(OutboxEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(ProductLikeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            applicationEventPublisher.publishEvent(
                    Objects.requireNonNull(
                            OutboxEvent.createProductLike(
                            "catalog-events",
                            event.productId().toString(),
                            payload)
                    )
            );
        }  catch (Exception e) {
            log.error("Failed to save event to outbox: {}", event, e);
            throw new RuntimeException("Outbox save failed", e);
        }

    }

    @Override
    public void publish(ProductUnLikeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            applicationEventPublisher.publishEvent(
                    Objects.requireNonNull(
                            OutboxEvent.createProductUnLike(
                                    "catalog-events",
                                    event.productId().toString(),
                                    payload)
                    )
            );
        }  catch (Exception e) {
            log.error("Failed to save event to outbox: {}", event, e);
            throw new RuntimeException("Outbox save failed", e);
        }
    }
}
