package com.loopers.domain.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScoreCalculator 테스트")
class ScoreCalculatorTest {

    private ScoreCalculator scoreCalculator;

    @BeforeEach
    void setUp() {
        scoreCalculator = new ScoreCalculator();
    }

    @Test
    @DisplayName("조회 이벤트 점수 계산")
    void calculateViewScore() {
        // when
        double score = scoreCalculator.calculateViewScore();

        // then
        assertThat(score).isEqualTo(0.1);
    }

    @Test
    @DisplayName("좋아요 이벤트 점수 계산")
    void calculateLikeScore_Like() {
        // when
        double score = scoreCalculator.calculateLikeScore(true);

        // then
        assertThat(score).isEqualTo(0.2);
    }

    @Test
    @DisplayName("좋아요 취소 이벤트 점수 계산")
    void calculateLikeScore_Unlike() {
        // when
        double score = scoreCalculator.calculateLikeScore(false);

        // then
        assertThat(score).isEqualTo(-0.2);
    }

    @Test
    @DisplayName("주문 이벤트 점수 계산")
    void calculateOrderScore() {
        // when
        double score = scoreCalculator.calculateOrderScore();

        // then
        assertThat(score).isEqualTo(0.6);
    }

    @Test
    @DisplayName("점수 가중치 검증")
    void verifyScoreWeights() {
        // given
        double viewScore = scoreCalculator.calculateViewScore();
        double likeScore = scoreCalculator.calculateLikeScore(true);
        double orderScore = scoreCalculator.calculateOrderScore();

        // then - 주문 > 좋아요 > 조회 순으로 가중치가 높아야 함
        assertThat(orderScore).isGreaterThan(likeScore);
        assertThat(likeScore).isGreaterThan(viewScore);
        
        // 전체 가중치 합이 1.0인지 검증
        assertThat(viewScore + likeScore + orderScore).isEqualTo(0.9);
    }
}
