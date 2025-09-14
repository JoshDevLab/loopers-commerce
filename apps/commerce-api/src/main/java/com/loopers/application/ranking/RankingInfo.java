package com.loopers.application.ranking;

import java.math.BigDecimal;

public record RankingInfo(
        Long rank,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal price,
        Double score
) {
}
