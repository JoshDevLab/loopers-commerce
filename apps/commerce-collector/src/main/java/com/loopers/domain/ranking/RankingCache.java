package com.loopers.domain.ranking;

import java.time.LocalDate;

public interface RankingCache {
    void incrementScore(Long productId, double score, LocalDate date);
    void carryOverScores(LocalDate fromDate, LocalDate toDate, double decayFactor);
}
