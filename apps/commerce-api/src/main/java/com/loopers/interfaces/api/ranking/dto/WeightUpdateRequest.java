package com.loopers.interfaces.api.ranking.dto;

import com.loopers.domain.ranking.WeightUpdateCommand;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeightUpdateRequest {

    @NotNull(message = "조회 가중치는 필수입니다")
    @DecimalMin(value = "0.0", message = "조회 가중치는 0 이상이어야 합니다")
    @DecimalMax(value = "1.0", message = "조회 가중치는 1 이하여야 합니다")
    private Double viewWeight;

    @NotNull(message = "좋아요 가중치는 필수입니다")
    @DecimalMin(value = "0.0", message = "좋아요 가중치는 0 이상이어야 합니다")
    @DecimalMax(value = "1.0", message = "좋아요 가중치는 1 이하여야 합니다")
    private Double likeWeight;

    @NotNull(message = "주문 가중치는 필수입니다")
    @DecimalMin(value = "0.0", message = "주문 가중치는 0 이상이어야 합니다")
    @DecimalMax(value = "1.0", message = "주문 가중치는 1 이하여야 합니다")
    private Double orderWeight;

    public WeightUpdateCommand toCommand() {
        return new WeightUpdateCommand(viewWeight, likeWeight, orderWeight);
    }
}
