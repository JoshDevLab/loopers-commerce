package com.loopers.infrastructure.productmetrics;

import com.loopers.domain.productmetrics.ProductMetrics;
import com.loopers.domain.productmetrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {
    private final ProductMetricsJpaRepository productMetricsJpaRepository;

    @Override
    public ProductMetrics save(ProductMetrics productMetrics) {
        return productMetricsJpaRepository.save(productMetrics);
    }

    @Override
    public Optional<ProductMetrics> findTodayProductMetric(Long productId) {
        ZonedDateTime today = ZonedDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault());
        return productMetricsJpaRepository.findByProductIdAndMetricDate(productId, today);
    }
}
