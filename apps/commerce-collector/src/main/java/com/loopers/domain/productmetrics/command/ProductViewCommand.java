package com.loopers.domain.productmetrics.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewCommand {
    String eventId;
    String eventType;
    Long productId;
    String aggregateId;
    ZonedDateTime metricDate;
    ZonedDateTime occurredAt;
}
