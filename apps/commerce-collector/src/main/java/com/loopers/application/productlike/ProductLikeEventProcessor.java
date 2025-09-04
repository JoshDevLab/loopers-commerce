package com.loopers.application.productlike;

import com.loopers.domain.productmetrics.ProductMetricsService;
import com.loopers.domain.productmetrics.command.ProductLikeMetricCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductLikeEventProcessor {
    private final ProductMetricsService productMetricsService;

    public void processEvent(ProductLikeMetricCommand command) {
        productMetricsService.metricProductLike(command);
    }

}
