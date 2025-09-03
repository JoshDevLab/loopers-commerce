package com.loopers.domain.auditlog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogCommand {
    private String eventId;
    private String eventType;
    private String aggregateId;
    private String rawPayload;
    private ZonedDateTime occurredAt;
    private String topicName;

    public AuditLog toEntity() {
        return AuditLog.of(eventId, eventType, aggregateId, rawPayload, occurredAt, topicName);
    }
}
