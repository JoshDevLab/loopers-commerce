package com.loopers.interfaces.consumer.productlike.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.domain.productmetrics.command.ProductLikeMetricCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductLikeEventDto {

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

    public boolean isProductLiked() {
        return "PRODUCT_LIKED".equals(eventType);
    }

    public boolean isProductUnliked() {
        return "PRODUCT_UNLIKED".equals(eventType);
    }

    public boolean isValid() {
        if (this.eventId == null || this.eventId.trim().isEmpty()) {
            log.warn("eventId가 없는 이벤트: {}", this);
            return false;
        }

        if (this.productId == null) {
            log.warn("productId가 없는 이벤트: {}", this);
            return false;
        }

        if (this.eventType == null || (!this.isProductLiked() && !this.isProductUnliked())) {
            log.warn("지원하지 않는 이벤트 타입: {}", this.eventType);
            return false;
        }

        return true;
    }

    /**
     * DTO를 Command 객체로 변환
     */
    public ProductLikeMetricCommand toCommand() {
        if (!isValid()) {
            log.warn("유효하지 않은 DTO로 Command 생성 불가: {}", this);
            return null;
        }

        try {
            return ProductLikeMetricCommand.builder()
                    .eventId(this.eventId)
                    .eventType(this.eventType)
                    .productId(this.productId)
                    .aggregateId(this.aggregateId != null ? this.aggregateId : this.productId.toString())
                    .metricDate(extractMetricDate())
                    .occurredAt(extractOccurredAt())
                    .build();

        } catch (Exception e) {
            log.error("DTO to Command 변환 실패: dto={}", this, e);
            return null;
        }
    }

    /**
     * 메트릭 날짜 추출 (타임스탬프에서 날짜 부분만)
     */
    private ZonedDateTime extractMetricDate() {
        return this.timestamp != null ?
                timestamp.toLocalDate().atStartOfDay(ZoneId.systemDefault()) :
                ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }

    /**
     * 발생 시각 추출
     */
    private ZonedDateTime extractOccurredAt() {
        return this.timestamp != null ? this.timestamp : ZonedDateTime.now();
    }

}
