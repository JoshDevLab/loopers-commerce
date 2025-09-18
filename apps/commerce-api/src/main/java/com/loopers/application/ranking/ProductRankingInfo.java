package com.loopers.application.ranking;

import com.loopers.domain.ranking.MvProductRankMonthly;
import com.loopers.domain.ranking.MvProductRankWeekly;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProductRankingInfo {
    private final Long productId;
    private final String productName;
    private final Long totalSales;
    private final Long totalViews;
    private final Long totalLikes;
    private final Double totalScore;
    private final Integer rankPosition;
    
    // 월간 랭킹용
    private final String yearMonth;
    
    // 주간 랭킹용
    private final LocalDate weekStartDate;
    private final LocalDate weekEndDate;
    
    // 일간 랭킹용
    private final LocalDate date;
    
    // 정적 팩토리 메서드들
    public static ProductRankingInfo fromMonthly(MvProductRankMonthly monthly) {
        return ProductRankingInfo.builder()
                .productId(monthly.getProductId())
                .productName(monthly.getProductName())
                .totalSales(monthly.getTotalSales())
                .totalViews(monthly.getTotalViews())
                .totalLikes(monthly.getTotalLikes())
                .totalScore(monthly.getTotalScore())
                .rankPosition(monthly.getRankPosition())
                .yearMonth(monthly.getReportMonth())
                .build();
    }
    
    public static ProductRankingInfo fromWeekly(MvProductRankWeekly weekly) {
        return ProductRankingInfo.builder()
                .productId(weekly.getProductId())
                .productName(weekly.getProductName())
                .totalSales(weekly.getTotalSales())
                .totalViews(weekly.getTotalViews())
                .totalLikes(weekly.getTotalLikes())
                .totalScore(weekly.getTotalScore())
                .rankPosition(weekly.getRankPosition())
                .weekStartDate(weekly.getWeekStartDate())
                .weekEndDate(weekly.getWeekEndDate())
                .build();
    }
    
    public static ProductRankingInfo fromDaily(RankingInfo rankingInfo, LocalDate date) {
        return ProductRankingInfo.builder()
                .productId(rankingInfo.productId())
                .productName(rankingInfo.productName())
                .totalSales(null) // 일간 랭킹에서는 없는 정보
                .totalViews(null) // 일간 랭킹에서는 없는 정보
                .totalLikes(null) // 일간 랭킹에서는 없는 정보
                .totalScore(rankingInfo.score())
                .rankPosition(Math.toIntExact(rankingInfo.rank()))
                .date(date)
                .build();
    }
}
