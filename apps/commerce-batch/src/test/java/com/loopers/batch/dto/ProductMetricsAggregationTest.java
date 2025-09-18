package com.loopers.batch.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMetricsAggregationTest {

    @Test
    void 빌더로_객체를_생성할_수_있다() {
        // given
        Long productId = 1L;
        String productName = "테스트 상품";
        Long totalSales = 100L;
        Long totalViews = 500L;
        Long totalLikes = 50L;
        Double totalScore = 250.0;

        // when
        ProductMetricsAggregation result = ProductMetricsAggregation.builder()
                .productId(productId)
                .productName(productName)
                .totalSales(totalSales)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalScore(totalScore)
                .build();

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo(productName);
        assertThat(result.getTotalSales()).isEqualTo(totalSales);
        assertThat(result.getTotalViews()).isEqualTo(totalViews);
        assertThat(result.getTotalLikes()).isEqualTo(totalLikes);
        assertThat(result.getTotalScore()).isEqualTo(totalScore);
    }

    @Test
    void 필수_필드만으로_객체를_생성할_수_있다() {
        // given & when
        ProductMetricsAggregation result = ProductMetricsAggregation.builder()
                .productId(1L)
                .productName("필수 필드 상품")
                .build();

        // then
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("필수 필드 상품");
        assertThat(result.getTotalSales()).isNull();
        assertThat(result.getTotalViews()).isNull();
        assertThat(result.getTotalLikes()).isNull();
        assertThat(result.getTotalScore()).isNull();
    }
}
