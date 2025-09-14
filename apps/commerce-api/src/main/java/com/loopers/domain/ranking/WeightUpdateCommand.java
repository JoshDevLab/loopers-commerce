package com.loopers.domain.ranking;

public record WeightUpdateCommand(
        double viewWeight,
        double likeWeight,
        double orderWeight
) {
}
