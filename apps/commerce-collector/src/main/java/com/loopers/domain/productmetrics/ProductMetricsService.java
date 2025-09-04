package com.loopers.domain.productmetrics;

import com.loopers.domain.productmetrics.command.ProductLikeMetricCommand;
import com.loopers.domain.productmetrics.command.ProductOrderMetricCommand;
import com.loopers.domain.productmetrics.command.ProductViewCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {
    private final ProductMetricsRepository productMetricsRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional
    public void metricProductLike(ProductLikeMetricCommand command) {
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElseGet(() -> productMetricsRepository.save(ProductMetrics.createNew(command.getProductId(), command.getMetricDate())));
        if (command.isLikeEvent()) {
            productMetrics.incrementLikeCount();
        } else {
            productMetrics.decrementLikeCount();
        }
        String cacheKey = "product:option:v1:" + command.getAggregateId();
        stringRedisTemplate.delete(cacheKey);
    }

    @Transactional
    public void metricProductOrder(ProductOrderMetricCommand command) {
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElseGet(() -> productMetricsRepository.save(ProductMetrics.createNew(command.getProductId(), command.getMetricDate())));
        productMetrics.incrementSalesCount();
    }

    @Transactional
    public void metricProductView(ProductViewCommand command) {
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElseGet(() -> productMetricsRepository.save(ProductMetrics.createNew(command.getProductId(), command.getMetricDate())));
        productMetrics.incrementViewCount();
    }
}
