package com.loopers.domain.stockadjusted;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAdjustedCommand {
    String eventId;
    String eventType;
    Long productOptionId;
    String aggregateId;
    ZonedDateTime metricDate;
    ZonedDateTime occurredAt;
}
