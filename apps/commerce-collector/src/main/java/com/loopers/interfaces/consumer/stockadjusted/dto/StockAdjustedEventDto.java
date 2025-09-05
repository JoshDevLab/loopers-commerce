package com.loopers.interfaces.consumer.stockadjusted.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.domain.stockadjusted.StockAdjustedCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustedEventDto {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("productOptionId")
    private Long productOptionId;

    @JsonProperty("aggregateId")
    private String aggregateId;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;

    public boolean isValid() {
        return StringUtils.hasText(eventId)
                && this.productOptionId != null
                && StringUtils.hasText(eventType);
    }

    private ZonedDateTime extractMetricDate() {
        return this.timestamp != null ?
                timestamp.toLocalDate().atStartOfDay(ZoneId.systemDefault()) :
                ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }

    /** 이벤트 발생 시간 */
    public ZonedDateTime extractOccurredAt() {
        return this.timestamp != null ? timestamp : ZonedDateTime.now();
    }

    public StockAdjustedCommand toCommand() {
        StockAdjustedCommand command = new StockAdjustedCommand();
        command.setEventId(this.eventId);
        command.setEventType(this.eventType);
        command.setProductOptionId(this.productOptionId);
        command.setAggregateId(this.aggregateId);
        command.setMetricDate(extractMetricDate());
        command.setOccurredAt(extractOccurredAt());
        return command;
    }
}
