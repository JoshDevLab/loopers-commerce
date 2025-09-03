package com.loopers.domain.productmetrics;

import java.util.Optional;

public interface ProductMetricsRepository {
    ProductMetrics save(ProductMetrics productMetrics);
    Optional<ProductMetrics> findTodayProductMetric(Long productId);
}
