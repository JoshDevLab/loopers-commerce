package com.loopers.batch.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductMetricsAggregation {
    private Long productId;
    private String productName;
    private Long totalSales;
    private Long totalViews;
    private Long totalLikes;
    private Double totalScore;
}
