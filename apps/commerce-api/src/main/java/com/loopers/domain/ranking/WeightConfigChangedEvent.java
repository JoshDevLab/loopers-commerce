package com.loopers.domain.ranking;

public record WeightConfigChangedEvent(
        double viewWeight,
        double likeWeight,
        double orderWeight
) {
}
