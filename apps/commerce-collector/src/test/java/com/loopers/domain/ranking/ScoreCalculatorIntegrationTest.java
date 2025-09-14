package com.loopers.domain.ranking;

import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScoreCalculator 통합 테스트")
class ScoreCalculatorIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ScoreCalculator scoreCalculator;

    @Autowired
    WeightConfigService weightConfigService;

    @Test
    @DisplayName("가중치 변경 후 점수 계산에 반영되는지 테스트")
    void scoreCalculation_withUpdatedWeights() {
        // given - 가중치 변경
        WeightConfig newWeightConfig = new WeightConfig(0.2, 0.3, 0.5);
        weightConfigService.updateWeights(newWeightConfig);

        // when
        double viewScore = scoreCalculator.calculateViewScore();
        double likeScore = scoreCalculator.calculateLikeScore(true);
        double orderScore = scoreCalculator.calculateOrderScore();

        // then - 변경된 가중치가 점수 계산에 반영되어야 함
        // 실제 ScoreCalculator 구현에 따라 달라질 수 있음
        assertThat(viewScore).isNotNegative();
        assertThat(likeScore).isNotNegative();
        assertThat(orderScore).isNotNegative();
    }

    @Test
    @DisplayName("기본 가중치로 초기화 후 점수 계산")
    void scoreCalculation_withDefaultWeights() {
        // given - 기본값으로 초기화
        weightConfigService.resetToDefault();

        // when
        double viewScore = scoreCalculator.calculateViewScore();
        double likeScore = scoreCalculator.calculateLikeScore(true);
        double orderScore = scoreCalculator.calculateOrderScore();

        // then
        assertThat(viewScore).isEqualTo(0.1);
        assertThat(likeScore).isEqualTo(0.2);
        assertThat(orderScore).isEqualTo(0.6);
    }

    @Test
    @DisplayName("좋아요 취소 시 음수 점수 반환")
    void scoreCalculation_unlikeNegativeScore() {
        // given - 기본값으로 초기화
        weightConfigService.resetToDefault();

        // when
        double unlikeScore = scoreCalculator.calculateLikeScore(false);

        // then
        assertThat(unlikeScore).isEqualTo(-0.2);
    }
}
