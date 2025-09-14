package com.loopers.domain.ranking;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WeightConfig 테스트")
class WeightConfigTest {

    @Test
    @DisplayName("기본 WeightConfig 생성")
    void getDefaultWeightConfig() {
        // when
        WeightConfig config = WeightConfig.getDefaultWeightConfig(0.1, 0.2, 0.7);

        // then
        assertThat(config.getViewWeight()).isEqualTo(0.1);
        assertThat(config.getLikeWeight()).isEqualTo(0.2);
        assertThat(config.getOrderWeight()).isEqualTo(0.7);
    }

    @Test
    @DisplayName("유효한 가중치 검증 - 성공")
    void validate_success() {
        // given
        double viewWeight = 0.1;
        double likeWeight = 0.3;
        double orderWeight = 0.6;

        // when & then - 예외가 발생하지 않아야 함
        WeightConfig.validate(viewWeight, likeWeight, orderWeight);
    }

    @Test
    @DisplayName("음수 가중치 검증 - 실패")
    void validate_negative_weights() {
        // given
        double viewWeight = -0.1;
        double likeWeight = 0.3;
        double orderWeight = 0.8;

        // when & then
        assertThatThrownBy(() -> WeightConfig.validate(viewWeight, likeWeight, orderWeight))
                .isInstanceOf(CoreException.class)
                .hasMessage("가중치는 음수일 수 없습니다.");
    }


    @Test
    @DisplayName("부동소수점 오차를 고려한 가중치 검증 - 성공")
    void validate_with_floating_point_precision() {
        // given - 부동소수점 연산으로 인한 미세한 오차
        double viewWeight = 0.1;
        double likeWeight = 0.3;
        double orderWeight = 0.59999999; // 실제 합: 0.99999999

        // when & then - 0.001 오차 범위 내에서 성공해야 함
        WeightConfig.validate(viewWeight, likeWeight, orderWeight);
    }


    @Test
    @DisplayName("하나의 가중치만 1.0인 경우")
    void validate_single_weight_one() {
        // given
        double viewWeight = 1.0;
        double likeWeight = 0.0;
        double orderWeight = 0.0;

        // when & then - 예외가 발생하지 않아야 함
        WeightConfig.validate(viewWeight, likeWeight, orderWeight);
    }
}
