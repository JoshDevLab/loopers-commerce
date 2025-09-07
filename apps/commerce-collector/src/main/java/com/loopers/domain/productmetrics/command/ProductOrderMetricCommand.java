package com.loopers.domain.productmetrics.command;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class ProductOrderMetricCommand {
    private final String eventId;
    private final String eventType;
    private final Long productId;
    private final String aggregateId;
    private final ZonedDateTime metricDate;
    private final ZonedDateTime occurredAt;
}
