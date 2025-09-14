package com.loopers.interfaces.api.ranking.dto;

import java.math.BigDecimal;

public record RankingResponse(
        Long rank,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal price,
        Double score
) {
}
