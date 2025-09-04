package com.loopers.interfaces.consumer.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.domain.productmetrics.command.ProductOrderMetricCommand;
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
public class ProductOrderEventDto {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("productIds")
    private List<Long> productIds;

    @JsonProperty("aggregateId")
    private String aggregateId;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;

    public Long getProductId() {
        return productIds != null && !productIds.isEmpty() ? productIds.get(0) : null;
    }

    public boolean isValid() {
        return StringUtils.hasText(eventId)
                && getProductId() != null
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

    public ProductOrderMetricCommand toCommand() {
        return ProductOrderMetricCommand.builder()
                .eventId(this.eventId)
                .eventType(this.eventType)
                .productId(getProductId())
                .aggregateId(this.aggregateId)
                .metricDate(extractMetricDate())
                .occurredAt(extractOccurredAt())
                .build();
    }
}
