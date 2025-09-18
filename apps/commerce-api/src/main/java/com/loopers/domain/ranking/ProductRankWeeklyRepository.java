package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public interface ProductRankWeeklyRepository {
    List<MvProductRankWeekly> findByWeekStartDateOrderByRankPosition(LocalDate weekStartDate);
    List<MvProductRankWeekly> findTop10ByWeekStartDateOrderByRankPosition(LocalDate weekStartDate);
    List<MvProductRankWeekly> findByProductIdAndWeekStartDateIn(Long productId, List<LocalDate> weekStartDates);
}
