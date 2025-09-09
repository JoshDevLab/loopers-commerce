package com.loopers.domain.ranking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RankingItem {
    private Long productId;
    private Double score;
    private Long rank;
}
