package com.loopers.domain.productmetrics;

import com.loopers.domain.productmetrics.command.ProductLikeMetricCommand;
import com.loopers.domain.productmetrics.command.ProductOrderMetricCommand;
import com.loopers.domain.productmetrics.command.ProductViewCommand;
import com.loopers.domain.ranking.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductMetricsService 테스트")
class ProductMetricsServiceTest {

    @InjectMocks
    private ProductMetricsService productMetricsService;

    @Mock
    private ProductMetricsRepository productMetricsRepository;
    
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    
    @Mock
    private RankingService rankingService;

    private Long productId;
    private ZonedDateTime testDate;
    private LocalDate testLocalDate;
    private ProductMetrics existingMetrics;

    @BeforeEach
    void setUp() {
        productId = 123L;
        testDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
        testLocalDate = testDate.toLocalDate();
        existingMetrics = ProductMetrics.createNew(productId, testDate);
    }

    @Test
    @DisplayName("상품 좋아요 메트릭 처리 - 기존 메트릭이 있는 경우")
    void metricProductLike_WithExistingMetrics() {
        // given
        ProductLikeMetricCommand command = ProductLikeMetricCommand.builder()
                .eventId("event-123")
                .eventType("PRODUCT_LIKED")
                .productId(productId)
                .aggregateId("product-123")
                .metricDate(testDate)
                .occurredAt(testDate)
                .build();

        given(productMetricsRepository.findTodayProductMetric(productId))
                .willReturn(Optional.of(existingMetrics));

        // when
        productMetricsService.metricProductLike(command);

        // then
        then(productMetricsRepository).should().findTodayProductMetric(productId);
        then(rankingService).should().updateProductLikeScore(productId, true, testLocalDate);
        then(stringRedisTemplate).should().delete("product:option:v1:product-123");
    }

    @Test
    @DisplayName("상품 좋아요 메트릭 처리 - 기존 메트릭이 없는 경우")
    void metricProductLike_WithoutExistingMetrics() {
        // given
        ProductLikeMetricCommand command = ProductLikeMetricCommand.builder()
                .eventId("event-123")
                .eventType("PRODUCT_LIKED")
                .productId(productId)
                .aggregateId("product-123")
                .metricDate(testDate)
                .occurredAt(testDate)
                .build();

        ProductMetrics newMetrics = ProductMetrics.createNew(productId, testDate);
        
        given(productMetricsRepository.findTodayProductMetric(productId))
                .willReturn(Optional.empty());
        given(productMetricsRepository.save(any(ProductMetrics.class)))
                .willReturn(newMetrics);

        // when
        productMetricsService.metricProductLike(command);

        // then
        then(productMetricsRepository).should().findTodayProductMetric(productId);
        then(productMetricsRepository).should().save(any(ProductMetrics.class));
        then(rankingService).should().updateProductLikeScore(productId, true, testLocalDate);
        then(stringRedisTemplate).should().delete("product:option:v1:product-123");
    }

    @Test
    @DisplayName("상품 좋아요 취소 메트릭 처리")
    void metricProductUnlike() {
        // given
        ProductLikeMetricCommand command = ProductLikeMetricCommand.builder()
                .eventId("event-123")
                .eventType("PRODUCT_UNLIKED")
                .productId(productId)
                .aggregateId("product-123")
                .metricDate(testDate)
                .occurredAt(testDate)
                .build();

        given(productMetricsRepository.findTodayProductMetric(productId))
                .willReturn(Optional.of(existingMetrics));

        // when
        productMetricsService.metricProductLike(command);

        // then
        then(rankingService).should().updateProductLikeScore(productId, false, testLocalDate);
    }

    @Test
    @DisplayName("상품 주문 메트릭 처리")
    void metricProductOrder() {
        // given
        ProductOrderMetricCommand command = ProductOrderMetricCommand.builder()
                .eventId("event-456")
                .eventType("PRODUCT_ORDERED")
                .productId(productId)
                .aggregateId("order-456")
                .metricDate(testDate)
                .occurredAt(testDate)
                .build();

        given(productMetricsRepository.findTodayProductMetric(productId))
                .willReturn(Optional.of(existingMetrics));

        // when
        productMetricsService.metricProductOrder(command);

        // then
        then(productMetricsRepository).should().findTodayProductMetric(productId);
        then(rankingService).should().updateProductOrderScore(productId, testLocalDate);
    }

    @Test
    @DisplayName("상품 조회 메트릭 처리")
    void metricProductView() {
        // given
        ProductViewCommand command = new ProductViewCommand(
                "event-789",
                "PRODUCT_VIEWED",
                productId,
                "product-789",
                testDate,
                testDate
        );

        given(productMetricsRepository.findTodayProductMetric(productId))
                .willReturn(Optional.of(existingMetrics));

        // when
        productMetricsService.metricProductView(command);

        // then
        then(productMetricsRepository).should().findTodayProductMetric(productId);
        then(rankingService).should().updateProductViewScore(productId, testLocalDate);
    }

    @Test
    @DisplayName("여러 메트릭이 연속으로 처리되는 경우")
    void multipleMetricsProcessing() {
        // given
        ProductLikeMetricCommand likeCommand = ProductLikeMetricCommand.builder()
                .eventId("event-1")
                .eventType("PRODUCT_LIKED")
                .productId(productId)
                .aggregateId("product-123")
                .metricDate(testDate)
                .occurredAt(testDate)
                .build();

        ProductViewCommand viewCommand = new ProductViewCommand(
                "event-2",
                "PRODUCT_VIEWED",
                productId,
                "product-123",
                testDate,
                testDate
        );

        given(productMetricsRepository.findTodayProductMetric(productId))
                .willReturn(Optional.of(existingMetrics));

        // when
        productMetricsService.metricProductLike(likeCommand);
        productMetricsService.metricProductView(viewCommand);

        // then
        then(rankingService).should().updateProductLikeScore(productId, true, testLocalDate);
        then(rankingService).should().updateProductViewScore(productId, testLocalDate);
        then(productMetricsRepository).should(times(2)).findTodayProductMetric(productId);
    }
}
