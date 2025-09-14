package com.loopers.interfaces.consumer.ranking;

import com.loopers.application.IdempotentEventProcessor;
import com.loopers.application.ranking.WeightConfigEventProcessor;
import com.loopers.interfaces.consumer.DltPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeightConfigEventConsumer 테스트")
class WeightConfigEventConsumerTest {

    @Mock
    WeightConfigEventProcessor weightConfigEventProcessor;
    
    @Mock
    IdempotentEventProcessor idempotentEventProcessor;
    
    @Mock
    DltPublisher dltPublisher;

    WeightConfigEventConsumer sut;
    
    @Test
    @DisplayName("유효한 이벤트 DTO 검증")
    void isValid() {
        // given
        sut = new WeightConfigEventConsumer(idempotentEventProcessor, dltPublisher, weightConfigEventProcessor);
        WeightConfigEventDto validEventDto = new WeightConfigEventDto(
                "event-123", "WEIGHT_CONFIG_CHANGED", "weight-config-1", 0.15, 0.25, 0.6
        );

        // when
        boolean result = sut.isValid(validEventDto);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("null 이벤트는 유효하지 않다")
    void isValid_nullEvent() {
        // given
        sut = new WeightConfigEventConsumer(idempotentEventProcessor, dltPublisher, weightConfigEventProcessor);

        // when
        boolean result = sut.isValid(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("가중치 합이 1.0이 아닌 이벤트는 유효하지 않다")
    void isValid_invalidWeightSum() {
        // given
        sut = new WeightConfigEventConsumer(idempotentEventProcessor, dltPublisher, weightConfigEventProcessor);
        WeightConfigEventDto invalidEventDto = new WeightConfigEventDto(
                "event-123", "WEIGHT_CONFIG_CHANGED", "weight-config-1", 0.2, 0.3, 0.6  // 합이 1.1
        );

        // when
        boolean result = sut.isValid(invalidEventDto);

        // then
        assertThat(result).isFalse();
    }
}
