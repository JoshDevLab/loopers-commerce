package com.loopers.domain.ranking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScoreCalculator {

    // 이벤트 가중치 설정
    private static final double VIEW_WEIGHT = 0.1;
    private static final double LIKE_WEIGHT = 0.2;
    private static final double ORDER_WEIGHT = 0.6;

    public double calculateViewScore() {
        return VIEW_WEIGHT * 1.0;
    }

    public double calculateLikeScore(boolean isLikeEvent) {
        return isLikeEvent ? LIKE_WEIGHT * 1.0 : -(LIKE_WEIGHT * 1.0);
    }

    public double calculateOrderScore() {
        return ORDER_WEIGHT * 1.0;
    }
}
