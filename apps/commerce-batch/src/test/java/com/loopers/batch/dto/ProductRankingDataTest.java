package com.loopers.batch.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRankingDataTest {

    @Test
    void 주간_랭킹_데이터를_생성할_수_있다() {
        // given
        Long productId = 1L;
        String productName = "테스트 상품";
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        LocalDate weekEnd = LocalDate.of(2024, 1, 7);

        // when
        ProductRankingData result = ProductRankingData.weeklyBuilder()
                .productId(productId)
                .productName(productName)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .totalSales(100L)
                .totalViews(500L)
                .totalLikes(50L)
                .totalScore(250.0)
                .rankPosition(1)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo(productName);
        assertThat(result.getWeekStartDate()).isEqualTo(weekStart);
        assertThat(result.getWeekEndDate()).isEqualTo(weekEnd);
        assertThat(result.getTotalSales()).isEqualTo(100L);
        assertThat(result.getRankPosition()).isEqualTo(1);
    }

    @Test
    void 월간_랭킹_데이터를_생성할_수_있다() {
        // given
        Long productId = 2L;
        String productName = "월간 테스트 상품";
        Integer year = 2024;
        Integer month = 1;

        // when
        ProductRankingData result = ProductRankingData.monthlyBuilder()
                .productId(productId)
                .productName(productName)
                .reportYear(year)
                .reportMonth(month)
                .reportMonthString("2024-01")
                .totalSales(300L)
                .totalViews(1500L)
                .totalLikes(150L)
                .totalScore(750.0)
                .rankPosition(1)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo(productName);
        assertThat(result.getReportYear()).isEqualTo(year);
        assertThat(result.getReportMonth()).isEqualTo("2024-01");
        assertThat(result.getTotalSales()).isEqualTo(300L);
        assertThat(result.getRankPosition()).isEqualTo(1);
    }
}
