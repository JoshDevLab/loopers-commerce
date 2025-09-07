package com.loopers.interfaces.consumer.auditlog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.domain.auditlog.AuditLogCommand;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Slf4j
@Data
@NoArgsConstructor
public class UniversalEventDto {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("aggregateId")
    private String aggregateId;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;

    private String rawPayload;

    private String topicName;

    public boolean isValid() {
        if (eventId == null || eventId.trim().isEmpty()) {
            log.warn("eventId가 없는 이벤트");
            return false;
        }

        if (eventType == null || eventType.trim().isEmpty()) {
            log.warn("eventType이 없는 이벤트: eventId={}", eventId);
            return false;
        }

        return true;
    }

    public ZonedDateTime getOccurredAt() {
        return timestamp != null ? timestamp : ZonedDateTime.now();
    }


    public AuditLogCommand toCommand() {
        return new AuditLogCommand(
                eventId,
                eventType,
                aggregateId,
                rawPayload,
                getOccurredAt(),
                topicName
        );
    }
}
