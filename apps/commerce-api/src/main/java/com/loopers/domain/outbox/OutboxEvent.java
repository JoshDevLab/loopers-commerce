package com.loopers.domain.outbox;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox_event",
        indexes = {
                @Index(name = "idx_published_created", columnList = "published, createdAt"),
                @Index(name = "idx_aggregate_created", columnList = "aggregateId, createdAt")
        })
public class OutboxEvent extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String eventId;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    /**
     * MySQL의 JSON 타입은 JPA에선 String으로 매핑하는 것이 일반적입니다.
     * (Hibernate 6.x 이상은 Json 지원 있지만, 호환성 위해 String으로 두는 편이 안정적)
     */
    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "json")
    private String payload;

    private String topicName;

    private String partitionKey;

    private Boolean published = false;

    private ZonedDateTime publishedAt;

    public static OutboxEvent createProductLike(String topicName, String aggregateId, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventId = UUID.randomUUID().toString();
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = EventType.PRODUCT_LIKED;
        outboxEvent.topicName = topicName;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    public static OutboxEvent createProductUnLike(String topicName, String aggregateId, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventId = UUID.randomUUID().toString();
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = EventType.PRODUCT_UNLIKED;
        outboxEvent.topicName = topicName;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    public static OutboxEvent createSales(String topicName, String aggregateId, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventId = UUID.randomUUID().toString();
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = EventType.ORDER_CREATED;
        outboxEvent.topicName = topicName;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    public static OutboxEvent createProductView(String topicName, String aggregateId, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventId = UUID.randomUUID().toString();
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = EventType.PRODUCT_VIEW;
        outboxEvent.topicName = topicName;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    public static OutboxEvent createStockAdjusted(String topicName, String aggregateId, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventId = UUID.randomUUID().toString();
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = EventType.STOCK_ADJUSTED;
        outboxEvent.topicName = topicName;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    public static OutboxEvent createWeighConfig(String topicName, String aggregateId, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventId = UUID.randomUUID().toString();
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = EventType.WEIGHT_CONFIG_CHANGED;
        outboxEvent.topicName = topicName;
        outboxEvent.payload = payload;
        return outboxEvent;
    }

    public enum EventType {
        ORDER_CREATED,
        PRODUCT_LIKED,
        PRODUCT_UNLIKED,
        PRODUCT_CHANGED, PRODUCT_VIEW, STOCK_ADJUSTED, WEIGHT_CONFIG_CHANGED,
    }
}
