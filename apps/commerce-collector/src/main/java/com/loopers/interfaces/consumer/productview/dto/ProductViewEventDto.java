package com.loopers.interfaces.consumer.productview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.domain.productmetrics.command.ProductViewCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductViewEventDto {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("aggregateId")
    private String aggregateId;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;

    public boolean isValid() {
        return StringUtils.hasText(eventId)
                && productId != null
                && StringUtils.hasText(eventType);
    }

    private ZonedDateTime extractMetricDate() {
        return this.timestamp != null ?
                timestamp.toLocalDate().atStartOfDay(ZoneId.systemDefault()) :
                ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }

    public ZonedDateTime extractOccurredAt() {
        return this.timestamp != null ? timestamp : ZonedDateTime.now();
    }

    public ProductViewCommand toCommand() {
        return new ProductViewCommand(
                this.eventId,
                this.eventType,
                this.productId,
                this.aggregateId,
                this.extractMetricDate(),
                this.extractOccurredAt()
        );
    }
}
