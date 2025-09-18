package com.loopers.batch.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
public class ProductRankingData {
    private Long productId;
    private String productName;
    private Long totalSales;
    private Long totalViews;
    private Long totalLikes;
    private Double totalScore;
    private Integer rankPosition;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    
    // 월간 랭킹용
    private Integer reportYear;
    private Integer reportMonth;
    private String reportMonthString; // YearMonth 형태 (예: 2024-01)
    
    // 주간 랭킹용
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    
    public static ProductRankingDataBuilder monthlyBuilder() {
        return ProductRankingData.builder();
    }
    
    public static ProductRankingDataBuilder weeklyBuilder() {
        return ProductRankingData.builder();
    }
    
    // 테스트 호환성을 위한 getter
    public String getReportMonth() {
        return reportMonthString;
    }
}
