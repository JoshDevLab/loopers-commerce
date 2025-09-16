package com.loopers.domain.ranking;

import java.util.List;

public interface ProductRankMonthlyRepository {
    List<MvProductRankMonthly> findByReportMonthOrderByRankPosition(String reportMonth);
    List<MvProductRankMonthly> findTop10ByReportMonthOrderByRankPosition(String reportMonth);
    List<MvProductRankMonthly> findByProductIdAndReportMonthIn(Long productId, List<String> reportMonths);
}
