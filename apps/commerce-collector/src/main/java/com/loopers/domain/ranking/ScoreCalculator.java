package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScoreCalculator {

    private final WeightConfigService weightConfigService;

    public double calculateViewScore() {
        return weightConfigService.getCurrentWeights().getViewWeight();
    }

    public double calculateLikeScore(boolean isLikeEvent) {
        double score = weightConfigService.getCurrentWeights().getLikeWeight();
        return isLikeEvent ? score : -score;
    }

    public double calculateOrderScore() {
        return weightConfigService.getCurrentWeights().getOrderWeight();
    }
}
