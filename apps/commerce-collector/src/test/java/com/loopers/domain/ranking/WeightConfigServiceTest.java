package com.loopers.domain.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeightConfigService 테스트 (Collector)")
class WeightConfigServiceTest {

    @Mock
    WeightCache weightCache;

    @InjectMocks
    WeightConfigService sut;

    @Test
    @DisplayName("가중치 업데이트 시 검증 후 캐시에 저장한다")
    void updateWeights() {
        // given
        WeightConfig weightConfig = new WeightConfig(0.1, 0.3, 0.6);

        // when
        sut.updateWeights(weightConfig);

        // then
        verify(weightCache, times(1)).saveWeightConfig(weightConfig);
    }

    @Test
    @DisplayName("현재 가중치 조회 시 캐시에서 데이터를 가져온다")
    void getCurrentWeights_fromCache() {
        // given
        WeightConfig cachedConfig = new WeightConfig(0.15, 0.25, 0.6);
        when(weightCache.getWeightConfig()).thenReturn(cachedConfig);

        // when
        WeightConfig result = sut.getCurrentWeights();

        // then
        assertThat(result).isSameAs(cachedConfig);
        verify(weightCache, times(1)).getWeightConfig();
    }

    @Test
    @DisplayName("캐시에 설정이 없을 때 기본값을 반환한다")
    void getCurrentWeights_defaultValues() {
        // given
        when(weightCache.getWeightConfig()).thenReturn(null);

        // when
        WeightConfig result = sut.getCurrentWeights();

        // then
        assertThat(result.getViewWeight()).isEqualTo(0.1);
        assertThat(result.getLikeWeight()).isEqualTo(0.2);
        assertThat(result.getOrderWeight()).isEqualTo(0.6);
        verify(weightCache, times(1)).getWeightConfig();
    }

    @Test
    @DisplayName("기본값으로 초기화 시 기본 가중치가 저장된다")
    void resetToDefault() {
        // when
        sut.resetToDefault();

        // then
        ArgumentCaptor<WeightConfig> captor = ArgumentCaptor.forClass(WeightConfig.class);
        verify(weightCache, times(1)).saveWeightConfig(captor.capture());

        WeightConfig savedConfig = captor.getValue();
        assertThat(savedConfig.getViewWeight()).isEqualTo(0.1);
        assertThat(savedConfig.getLikeWeight()).isEqualTo(0.2);
        assertThat(savedConfig.getOrderWeight()).isEqualTo(0.6);
    }
}
