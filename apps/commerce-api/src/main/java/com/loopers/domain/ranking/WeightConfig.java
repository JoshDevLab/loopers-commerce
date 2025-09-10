package com.loopers.domain.ranking;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeightConfig {
    private double viewWeight;
    private double likeWeight;
    private double orderWeight;

    public static WeightConfig getDefaultWeightConfig(double defaultViewWeight, double defaultLikeWeight, double defaultOrderWeight) {
        return new WeightConfig(defaultViewWeight, defaultLikeWeight, defaultOrderWeight);
    }

    public static void validate(double viewWeight, double likeWeight, double orderWeight) {
        if (viewWeight < 0 || likeWeight < 0 || orderWeight < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가중치는 음수일 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return String.format("WeightConfig{view=%.3f, like=%.3f, order=%.3f}",
                viewWeight, likeWeight, orderWeight);
    }
}
