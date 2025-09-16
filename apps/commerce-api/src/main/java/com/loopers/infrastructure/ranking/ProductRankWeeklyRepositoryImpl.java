package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankWeekly;
import com.loopers.domain.ranking.ProductRankWeeklyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRankWeeklyRepositoryImpl implements ProductRankWeeklyRepository {
    
    private final ProductRankWeeklyJpaRepository jpaRepository;
    
    @Override
    public List<MvProductRankWeekly> findByWeekStartDateOrderByRankPosition(LocalDate weekStartDate) {
        return jpaRepository.findByWeekStartDateOrderByRankPosition(weekStartDate);
    }
    
    @Override
    public List<MvProductRankWeekly> findTop10ByWeekStartDateOrderByRankPosition(LocalDate weekStartDate) {
        return jpaRepository.findTop10ByWeekStartDateOrderByRankPosition(weekStartDate);
    }
    
    @Override
    public List<MvProductRankWeekly> findByProductIdAndWeekStartDateIn(Long productId, List<LocalDate> weekStartDates) {
        return jpaRepository.findByProductIdAndWeekStartDateInOrderByWeekStartDate(productId, weekStartDates);
    }
}
