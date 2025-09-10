package com.loopers.interfaces.api.ranking.dto;

import com.loopers.domain.ranking.WeightConfigInfo;

public record WeightConfigResponse(double viewWeight, double likeWeight, double orderWeight) {
    public static WeightConfigResponse from(WeightConfigInfo info) {
        return new WeightConfigResponse(
                info.viewWeight(),
                info.likeWeight(),
                info.orderWeight()
        );
    }
}
