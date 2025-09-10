package com.loopers.domain.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WeightConfig 테스트 (Collector)")
class WeightConfigTest {

    @Test
    @DisplayName("WeightConfig 생성")
    void createWeightConfig() {
        // when
        WeightConfig config = new WeightConfig(0.1, 0.2, 0.7);

        // then
        assertThat(config.getViewWeight()).isEqualTo(0.1);
        assertThat(config.getLikeWeight()).isEqualTo(0.2);
        assertThat(config.getOrderWeight()).isEqualTo(0.7);
    }

    @Test
    @DisplayName("유효한 가중치 검증 - 성공")
    void validate_success() {
        // given
        WeightConfig config = new WeightConfig(0.1, 0.3, 0.6);

        // when & then - 예외가 발생하지 않아야 함
        config.validate();
    }

    @Test
    @DisplayName("음수 가중치 검증 - 실패")
    void validate_negative_weights() {
        // given
        WeightConfig config = new WeightConfig(-0.1, 0.3, 0.8);

        // when & then
        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가중치는 음수일 수 없습니다.");
    }


    @Test
    @DisplayName("toString 메서드 테스트")
    void toStringTest() {
        // given
        WeightConfig config = new WeightConfig(0.1, 0.2, 0.7);

        // when
        String result = config.toString();

        // then
        assertThat(result).contains("view=0.100");
        assertThat(result).contains("like=0.200");
        assertThat(result).contains("order=0.700");
    }
}
