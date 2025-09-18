package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import com.loopers.domain.ranking.ProductRankMonthlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRankMonthlyRepositoryImpl implements ProductRankMonthlyRepository {
    
    private final ProductRankMonthlyJpaRepository jpaRepository;
    
    @Override
    public List<MvProductRankMonthly> findByReportMonthOrderByRankPosition(String reportMonth) {
        return jpaRepository.findByReportMonthOrderByRankPosition(reportMonth);
    }
    
    @Override
    public List<MvProductRankMonthly> findTop10ByReportMonthOrderByRankPosition(String reportMonth) {
        return jpaRepository.findTop10ByReportMonthOrderByRankPosition(reportMonth);
    }
    
    @Override
    public List<MvProductRankMonthly> findByProductIdAndReportMonthIn(Long productId, List<String> reportMonths) {
        return jpaRepository.findByProductIdAndReportMonthInOrderByReportMonth(productId, reportMonths);
    }
}
