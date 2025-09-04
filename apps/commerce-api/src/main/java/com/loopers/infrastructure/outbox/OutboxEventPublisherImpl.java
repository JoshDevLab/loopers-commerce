package com.loopers.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductViewEvent;
import com.loopers.domain.order.OrderCreatedEvent;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventPublisher;
import com.loopers.domain.product.like.ProductLikeEvent;
import com.loopers.domain.product.like.ProductUnLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventPublisherImpl implements OutboxEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(OrderCreatedEvent event) {
        try {
            for (Long productId : event.productIds()) {
                // 각 productId별로 개별 이벤트 객체 생성
                OrderCreatedEvent individualEvent = new OrderCreatedEvent(
                        event.orderId(),
                        event.orderItemCommands(),
                        event.usedPoint(),
                        event.userCouponId(),
                        event.paidAmount(),
                        event.userPk(),
                        event.discountAmount(),
                        List.of(productId)  // 단일 productId만 포함
                );

                String payload = objectMapper.writeValueAsString(individualEvent);
                applicationEventPublisher.publishEvent(
                        Objects.requireNonNull(
                                OutboxEvent.createSales(
                                        "product-order-events",
                                        productId.toString(),
                                        payload)
                        )
                );
            }
        } catch (Exception e) {
            log.error("Failed to save event to outbox: {}", event, e);
            throw new RuntimeException("Outbox save failed", e);
        }
    }

    @Override
    public void publish(ProductLikeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            applicationEventPublisher.publishEvent(
                    Objects.requireNonNull(
                            OutboxEvent.createProductLike(
                            "product-like-events",
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
                                    "product-like-events",
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
    public void publish(ProductViewEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            applicationEventPublisher.publishEvent(
                    Objects.requireNonNull(
                            OutboxEvent.createProductView(
                                    "product-view-events",
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
