package com.loopers.domain.productmetrics.command;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class ProductLikeMetricCommand {

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
}
