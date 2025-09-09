package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public interface RankingCache {
    List<RankingItem> getRankings(LocalDate date, long offset, long count);
    Long getProductRank(Long productId, LocalDate date);
    long getTotalCount(LocalDate date);
}
