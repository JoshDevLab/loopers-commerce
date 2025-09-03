package com.loopers.application.productlike;

import com.loopers.domain.command.ProductLikeCommand;
import com.loopers.domain.productmetrics.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductLikeEventProcessor {
    private final ProductMetricsService productMetricsService;

    public void processEvent(ProductLikeCommand command) {
        productMetricsService.metricProductLike(command);
    }

}
