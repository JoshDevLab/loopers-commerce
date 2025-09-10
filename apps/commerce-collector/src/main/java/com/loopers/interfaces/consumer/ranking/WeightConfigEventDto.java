package com.loopers.interfaces.consumer.ranking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.domain.ranking.WeightConfigUpdateCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeightConfigEventDto {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("aggregateId")
    private String aggregateId;

    @JsonProperty("viewWeight")
    private Double viewWeight;

    @JsonProperty("likeWeight")
    private Double likeWeight;

    @JsonProperty("orderWeight")
    private Double orderWeight;

    public boolean isValid() {
        if (this.eventId == null || this.eventId.trim().isEmpty()) {
            log.warn("eventId가 없는 가중치 변경 이벤트: {}", this);
            return false;
        }

        if (this.viewWeight == null || this.likeWeight == null || this.orderWeight == null) {
            log.warn("가중치 값이 누락된 이벤트: {}", this);
            return false;
        }

        if (this.viewWeight < 0 || this.likeWeight < 0 || this.orderWeight < 0) {
            log.warn("음수 가중치가 포함된 이벤트: {}", this);
            return false;
        }

        double sum = this.viewWeight + this.likeWeight + this.orderWeight;
        if (Math.abs(sum - 1.0) > 0.001) {
            log.warn("가중치 합이 1.0이 아닌 이벤트: sum={}, event={}", sum, this);
            return false;
        }

        return true;
    }

    public WeightConfigUpdateCommand toCommand() {
        if (!isValid()) {
            log.warn("유효하지 않은 DTO로 Command 생성 불가: {}", this);
            return null;
        }

        try {
            return WeightConfigUpdateCommand.builder()
                    .eventId(this.eventId)
                    .viewWeight(this.viewWeight)
                    .likeWeight(this.likeWeight)
                    .orderWeight(this.orderWeight)
                    .occurredAt(ZonedDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("DTO to Command 변환 실패: dto={}", this, e);
            return null;
        }
    }
}
