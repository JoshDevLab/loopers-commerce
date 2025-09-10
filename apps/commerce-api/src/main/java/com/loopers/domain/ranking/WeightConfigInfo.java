package com.loopers.domain.ranking;

public record WeightConfigInfo(
        double viewWeight,
        double likeWeight,
        double orderWeight
) {
}
