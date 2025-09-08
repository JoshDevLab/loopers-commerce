package com.loopers.domain.productmetrics;

import com.loopers.domain.productmetrics.command.ProductLikeMetricCommand;
import com.loopers.domain.productmetrics.command.ProductOrderMetricCommand;
import com.loopers.domain.productmetrics.command.ProductViewCommand;
import com.loopers.domain.ranking.RankingService;  // 캐시 대신 서비스 사용
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {
    private final ProductMetricsRepository productMetricsRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RankingService rankingService;

    @Transactional
    public void metricProductLike(ProductLikeMetricCommand command) {
        // 기존 ProductMetrics 업데이트
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElseGet(() -> productMetricsRepository.save(ProductMetrics.createNew(command.getProductId(), command.getMetricDate())));

        if (command.isLikeEvent()) {
            productMetrics.incrementLikeCount();
        } else {
            productMetrics.decrementLikeCount();
        }

        // 랭킹 점수 업데이트
        LocalDate metricDate = command.getMetricDate().toLocalDate();
        rankingService.updateProductLikeScore(command.getProductId(), command.isLikeEvent(), metricDate);

        // 기존 캐시 삭제
        String cacheKey = "product:option:v1:" + command.getAggregateId();
        stringRedisTemplate.delete(cacheKey);
    }

    @Transactional
    public void metricProductOrder(ProductOrderMetricCommand command) {
        // 기존 ProductMetrics 업데이트
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElseGet(() -> productMetricsRepository.save(ProductMetrics.createNew(command.getProductId(), command.getMetricDate())));

        productMetrics.incrementSalesCount();

        // 랭킹 점수 업데이트
        LocalDate metricDate = command.getMetricDate().toLocalDate();
        rankingService.updateProductOrderScore(command.getProductId(), metricDate);
    }

    @Transactional
    public void metricProductView(ProductViewCommand command) {
        // 기존 ProductMetrics 업데이트
        ProductMetrics productMetrics = productMetricsRepository.findTodayProductMetric(command.getProductId())
                .orElseGet(() -> productMetricsRepository.save(ProductMetrics.createNew(command.getProductId(), command.getMetricDate())));

        productMetrics.incrementViewCount();

        // 랭킹 점수 업데이트
        LocalDate metricDate = command.getMetricDate().toLocalDate();
        rankingService.updateProductViewScore(command.getProductId(), metricDate);
    }
}
