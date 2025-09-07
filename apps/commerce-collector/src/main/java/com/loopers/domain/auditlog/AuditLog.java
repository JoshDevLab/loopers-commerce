package com.loopers.domain.auditlog;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    private String eventId;
    private String eventType;
    private String aggregateId;
    private String payload;
    private ZonedDateTime occurredAt;
    private String topicName;

    @Builder
    private AuditLog(String eventId,
                     String eventType,
                     String aggregateId,
                     String payload,
                     ZonedDateTime occurredAt,
                     String topicName) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.topicName = topicName;
    }

    public static AuditLog of(String eventId, String eventType, String aggregateId, String payload, ZonedDateTime occurredAt, String topicName) {
        return AuditLog.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateId(aggregateId)
                .payload(payload)
                .occurredAt(occurredAt)
                .topicName(topicName)
                .build();
    }
}
