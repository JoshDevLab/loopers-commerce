package com.loopers.domain.ranking;

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

    public void validate() {
        if (viewWeight < 0 || likeWeight < 0 || orderWeight < 0) {
            throw new IllegalArgumentException("가중치는 음수일 수 없습니다.");
        }

        double sum = viewWeight + likeWeight + orderWeight;
        if (Math.abs(sum - 1.0) > 0.001) { // 부동소수점 오차 고려
            throw new IllegalArgumentException("가중치의 합은 1.0이어야 합니다. 현재 합계: " + sum);
        }
    }

    @Override
    public String toString() {
        return String.format("WeightConfig{view=%.3f, like=%.3f, order=%.3f}",
                viewWeight, likeWeight, orderWeight);
    }
}
