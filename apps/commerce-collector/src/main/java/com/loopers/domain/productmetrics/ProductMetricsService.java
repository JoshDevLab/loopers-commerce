package com.loopers.domain.productmetrics;

import com.loopers.domain.command.ProductLikeCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {
    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void metricProductLike(ProductLikeCommand command) {
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElse(productMetricsRepository.save(ProductMetrics.createToday(command.getProductId())));
        productMetrics.incrementLikeCount();
    }
}
