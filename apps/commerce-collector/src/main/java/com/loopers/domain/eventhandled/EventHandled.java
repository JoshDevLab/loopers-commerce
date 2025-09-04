package com.loopers.domain.eventhandled;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "event_handled",
        indexes = {
                @Index(name = "idx_event_id", columnList = "eventId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_id", columnNames = {"eventId"})
        }
)
public class EventHandled extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String eventId;

    private String eventType;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingResult processingResult;

    @Column(length = 1000)
    private String errorMessage;

    public enum ProcessingResult {
        IN_PROGRESS, SUCCESS, FAILURE
    }

    @Builder
    private EventHandled(String eventId, String eventType,
                         String aggregateId, ProcessingResult processingResult,
                         String errorMessage) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.processingResult = processingResult;
        this.errorMessage = errorMessage;
    }

    // 팩토리
    public static EventHandled inProgress(String eventId, String eventType, String aggregateId) {
        return EventHandled.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateId(aggregateId)
                .processingResult(ProcessingResult.IN_PROGRESS)
                .build();
    }

    public void markSuccess() {
        this.processingResult = ProcessingResult.SUCCESS;
        this.errorMessage = null;
    }

    public void markFailure(String msg) {
        this.processingResult = ProcessingResult.FAILURE;
        this.errorMessage = truncate(msg);
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > 1000 ? s.substring(0, 997) + "..." : s;
    }
}
