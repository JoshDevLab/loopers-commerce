package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankWeekly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductRankWeeklyJpaRepository extends JpaRepository<MvProductRankWeekly, Long> {
    
    List<MvProductRankWeekly> findByWeekStartDateOrderByRankPosition(LocalDate weekStartDate);
    
    @Query("SELECT p FROM MvProductRankWeekly p WHERE p.weekStartDate = :weekStartDate ORDER BY p.rankPosition LIMIT 10")
    List<MvProductRankWeekly> findTop10ByWeekStartDateOrderByRankPosition(@Param("weekStartDate") LocalDate weekStartDate);
    
    List<MvProductRankWeekly> findByProductIdAndWeekStartDateInOrderByWeekStartDate(Long productId, List<LocalDate> weekStartDates);
}
