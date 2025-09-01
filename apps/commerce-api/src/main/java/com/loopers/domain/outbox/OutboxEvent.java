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

    public void markAsPublished() {
        this.published = true;
        this.publishedAt = ZonedDateTime.now();
    }

    public boolean isPublished() {
        return Boolean.TRUE.equals(this.published);
    }

    public enum EventType {
        ORDER_CREATED,
        PRODUCT_LIKED,
        PRODUCT_UNLIKED,
        PAYMENT_CREATED,
        PRODUCT_CHANGED,
    }
}
