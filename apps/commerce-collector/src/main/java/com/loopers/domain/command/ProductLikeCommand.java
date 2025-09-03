package com.loopers.domain.command;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
@Builder
public class ProductLikeCommand {

    private final String eventId;
    private final String eventType;
    private final Long productId;
    private final String aggregateId;
    private final ZonedDateTime metricDate;
    private final ZonedDateTime occurredAt;

    public boolean isLikeEvent() {
        return "PRODUCT_LIKED".equals(eventType);
    }

    public boolean isUnlikeEvent() {
        return "PRODUCT_UNLIKED".equals(eventType);
    }

    public boolean isValidForProcessing() {
        return eventId != null &&
                productId != null &&
                eventType != null &&
                metricDate != null &&
                (isLikeEvent() || isUnlikeEvent());
    }

    public String toAuditPayload() {
        return String.format("{\"eventId\":\"%s\",\"productId\":%d,\"eventType\":\"%s\",\"occurredAt\":\"%s\"}",
                eventId, productId, eventType, occurredAt);
    }
}
