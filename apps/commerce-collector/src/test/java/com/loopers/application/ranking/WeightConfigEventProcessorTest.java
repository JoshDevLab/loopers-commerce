package com.loopers.application.ranking;

import com.loopers.domain.ranking.WeightConfig;
import com.loopers.domain.ranking.WeightConfigService;
import com.loopers.domain.ranking.WeightConfigUpdateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeightConfigEventProcessor 테스트")
class WeightConfigEventProcessorTest {

    @Mock
    WeightConfigService weightConfigService;

    @InjectMocks
    WeightConfigEventProcessor sut;

    @Test
    @DisplayName("가중치 변경 이벤트 처리 시 WeightConfigService를 호출한다")
    void processEvent() {
        // given
        WeightConfigUpdateCommand command = WeightConfigUpdateCommand.builder()
                .eventId("event-123")
                .viewWeight(0.15)
                .likeWeight(0.25)
                .orderWeight(0.6)
                .occurredAt(ZonedDateTime.now())
                .build();

        // when
        sut.processEvent(command);

        // then
        ArgumentCaptor<WeightConfig> captor = ArgumentCaptor.forClass(WeightConfig.class);
        verify(weightConfigService, times(1)).updateWeights(captor.capture());

        WeightConfig weightConfig = captor.getValue();
        assertThat(weightConfig.getViewWeight()).isEqualTo(0.15);
        assertThat(weightConfig.getLikeWeight()).isEqualTo(0.25);
        assertThat(weightConfig.getOrderWeight()).isEqualTo(0.6);
    }

    @Test
    @DisplayName("null 이벤트 처리 시 서비스 호출하지 않는다")
    void processEvent_nullCommand() {
        // when
        sut.processEvent(null);

        // then
        verify(weightConfigService, never()).updateWeights(any());
    }

    @Test
    @DisplayName("이벤트 ID가 null인 경우에도 처리한다")
    void processEvent_nullEventId() {
        // given
        WeightConfigUpdateCommand command = WeightConfigUpdateCommand.builder()
                .eventId(null)
                .viewWeight(0.2)
                .likeWeight(0.3)
                .orderWeight(0.5)
                .occurredAt(ZonedDateTime.now())
                .build();

        // when
        sut.processEvent(command);

        // then
        ArgumentCaptor<WeightConfig> captor = ArgumentCaptor.forClass(WeightConfig.class);
        verify(weightConfigService, times(1)).updateWeights(captor.capture());

        WeightConfig weightConfig = captor.getValue();
        assertThat(weightConfig.getViewWeight()).isEqualTo(0.2);
        assertThat(weightConfig.getLikeWeight()).isEqualTo(0.3);
        assertThat(weightConfig.getOrderWeight()).isEqualTo(0.5);
    }
}
