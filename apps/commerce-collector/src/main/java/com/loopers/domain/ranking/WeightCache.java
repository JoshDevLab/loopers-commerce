package com.loopers.domain.ranking;

public interface WeightCache {
    void saveWeightConfig(WeightConfig weightConfig);
    WeightConfig getWeightConfig();
    boolean hasWeightConfig();
}
