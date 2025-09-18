package com.loopers.interfaces.api.ranking.dto;

import com.loopers.application.ranking.ProductRankingInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProductRankingResponse {
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
    
    /**
     * ProductRankingInfo를 ProductRankingResponse로 변환하는 정적 팩토리 메서드
     */
    public static ProductRankingResponse from(ProductRankingInfo info) {
        return ProductRankingResponse.builder()
                .productId(info.getProductId())
                .productName(info.getProductName())
                .totalSales(info.getTotalSales())
                .totalViews(info.getTotalViews())
                .totalLikes(info.getTotalLikes())
                .totalScore(info.getTotalScore())
                .rankPosition(info.getRankPosition())
                .yearMonth(info.getYearMonth())
                .weekStartDate(info.getWeekStartDate())
                .weekEndDate(info.getWeekEndDate())
                .date(info.getDate())
                .build();
    }
    
    /**
     * 여러 ProductRankingInfo를 ProductRankingResponse 리스트로 변환하는 편의 메서드
     */
    public static List<ProductRankingResponse> fromList(List<ProductRankingInfo> infos) {
        return infos.stream()
                .map(ProductRankingResponse::from)
                .toList();
    }
}
