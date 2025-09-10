package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeightCache;
import com.loopers.domain.ranking.WeightConfig;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WeightCacheImpl 통합 테스트")
class WeightCacheImplTest extends IntegrationTestSupport {

    @Autowired
    WeightCache weightCache;

    @Test
    @DisplayName("가중치 설정 저장 및 조회")
    void saveAndGetWeightConfig() {
        // given
        WeightConfig config = new WeightConfig(0.15, 0.25, 0.6);

        // when
        weightCache.saveWeightConfig(config);
        WeightConfig retrieved = weightCache.getWeightConfig();

        // then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getViewWeight()).isEqualTo(0.15);
        assertThat(retrieved.getLikeWeight()).isEqualTo(0.25);
        assertThat(retrieved.getOrderWeight()).isEqualTo(0.6);
    }

    @Test
    @DisplayName("저장된 가중치 설정이 없을 때 null 반환")
    void getWeightConfig_notExists() {
        // when
        WeightConfig result = weightCache.getWeightConfig();

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("가중치 설정 덮어쓰기")
    void overwriteWeightConfig() {
        // given
        WeightConfig firstConfig = new WeightConfig(0.1, 0.2, 0.7);
        WeightConfig secondConfig = new WeightConfig(0.2, 0.3, 0.5);

        // when
        weightCache.saveWeightConfig(firstConfig);
        weightCache.saveWeightConfig(secondConfig);
        WeightConfig retrieved = weightCache.getWeightConfig();

        // then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getViewWeight()).isEqualTo(0.2);
        assertThat(retrieved.getLikeWeight()).isEqualTo(0.3);
        assertThat(retrieved.getOrderWeight()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("부동소수점 정밀도 테스트")
    void floatingPointPrecision() {
        // given
        WeightConfig config = new WeightConfig(0.123456789, 0.234567891, 0.641975320);

        // when
        weightCache.saveWeightConfig(config);
        WeightConfig retrieved = weightCache.getWeightConfig();

        // then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getViewWeight()).isEqualTo(0.123456789);
        assertThat(retrieved.getLikeWeight()).isEqualTo(0.234567891);
        assertThat(retrieved.getOrderWeight()).isEqualTo(0.641975320);
    }
}
