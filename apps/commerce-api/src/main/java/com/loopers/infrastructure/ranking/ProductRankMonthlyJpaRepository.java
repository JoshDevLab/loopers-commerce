package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRankMonthlyJpaRepository extends JpaRepository<MvProductRankMonthly, Long> {
    
    List<MvProductRankMonthly> findByReportMonthOrderByRankPosition(String reportMonth);
    
    @Query("SELECT p FROM MvProductRankMonthly p WHERE p.reportMonth = :reportMonth ORDER BY p.rankPosition LIMIT 10")
    List<MvProductRankMonthly> findTop10ByReportMonthOrderByRankPosition(@Param("reportMonth") String reportMonth);
    
    List<MvProductRankMonthly> findByProductIdAndReportMonthInOrderByReportMonth(Long productId, List<String> reportMonths);
}
