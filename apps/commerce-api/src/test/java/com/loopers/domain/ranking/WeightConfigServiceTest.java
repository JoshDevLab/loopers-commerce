package com.loopers.domain.ranking;

import com.loopers.domain.outbox.OutboxEventPublisher;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeightConfigService 테스트")
class WeightConfigServiceTest {

    @Mock
    OutboxEventPublisher eventPublisher;

    @Mock
    WeightConfigCache weightConfigCache;

    @InjectMocks
    WeightConfigService sut;

    @Test
    @DisplayName("유효한 가중치로 업데이트 시 이벤트가 발행된다")
    void updateWeights_validWeights() {
        // given
        double viewWeight = 0.1;
        double likeWeight = 0.3;
        double orderWeight = 0.6;

        // when
        sut.updateWeights(viewWeight, likeWeight, orderWeight);

        // then
        ArgumentCaptor<WeightConfigChangedEvent> captor = ArgumentCaptor.forClass(WeightConfigChangedEvent.class);
        verify(eventPublisher, times(1)).publish(captor.capture());

        WeightConfigChangedEvent event = captor.getValue();
        assertThat(event.viewWeight()).isEqualTo(viewWeight);
        assertThat(event.likeWeight()).isEqualTo(likeWeight);
        assertThat(event.orderWeight()).isEqualTo(orderWeight);
    }

    @Test
    @DisplayName("음수 가중치로 업데이트 시 예외가 발생한다")
    void updateWeights_negativeWeights_throwsException() {
        // given
        double viewWeight = -0.1;
        double likeWeight = 0.3;
        double orderWeight = 0.8;

        // when & then
        assertThatThrownBy(() -> sut.updateWeights(viewWeight, likeWeight, orderWeight))
                .isInstanceOf(CoreException.class)
                .hasMessage("가중치는 음수일 수 없습니다.");

        verify(eventPublisher, never()).publish(any(WeightConfigChangedEvent.class));
    }

    @Test
    @DisplayName("기본값으로 초기화 시 기본 가중치 이벤트가 발행된다")
    void resetToDefault() {
        // given
        String reason = "관리자 초기화";

        // when
        sut.resetToDefault(reason);

        // then
        ArgumentCaptor<WeightConfigChangedEvent> captor = ArgumentCaptor.forClass(WeightConfigChangedEvent.class);
        verify(eventPublisher, times(1)).publish(captor.capture());

        WeightConfigChangedEvent event = captor.getValue();
        assertThat(event.viewWeight()).isEqualTo(0.1);
        assertThat(event.likeWeight()).isEqualTo(0.2);
        assertThat(event.orderWeight()).isEqualTo(0.6);
    }

    @Test
    @DisplayName("현재 가중치 조회 시 캐시에서 설정을 가져온다")
    void getCurrentWeights_fromCache() {
        // given
        WeightConfig cachedConfig = new WeightConfig(0.15, 0.25, 0.6);
        when(weightConfigCache.getWeightConfig()).thenReturn(cachedConfig);

        // when
        WeightConfigInfo result = sut.getCurrentWeights();

        // then
        assertThat(result.viewWeight()).isEqualTo(0.15);
        assertThat(result.likeWeight()).isEqualTo(0.25);
        assertThat(result.orderWeight()).isEqualTo(0.6);
        verify(weightConfigCache, times(1)).getWeightConfig();
    }

    @Test
    @DisplayName("캐시에 설정이 없을 때 기본값을 반환한다")
    void getCurrentWeights_defaultValues() {
        // given
        when(weightConfigCache.getWeightConfig()).thenReturn(null);

        // when
        WeightConfigInfo result = sut.getCurrentWeights();

        // then
        assertThat(result.viewWeight()).isEqualTo(0.1);
        assertThat(result.likeWeight()).isEqualTo(0.2);
        assertThat(result.orderWeight()).isEqualTo(0.6);
        verify(weightConfigCache, times(1)).getWeightConfig();
    }
}
