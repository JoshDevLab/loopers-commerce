package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingCache rankingCache;

    public List<RankingItem> getRankings(LocalDate date, long offset, long count) {
        return rankingCache.getRankings(date, offset, count);
    }

    public Long getProductRank(Long productId, LocalDate date) {
        return rankingCache.getProductRank(productId, date);
    }

    public long getTotalCount(LocalDate date) {
        return rankingCache.getTotalCount(date);
    }
}
