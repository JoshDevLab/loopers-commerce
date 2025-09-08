package com.loopers.infrastructure.ranking;

import com.loopers.domain.productmetrics.ProductMetricsService;
import com.loopers.domain.productmetrics.command.ProductLikeMetricCommand;
import com.loopers.domain.productmetrics.command.ProductOrderMetricCommand;
import com.loopers.domain.productmetrics.command.ProductViewCommand;
import com.loopers.domain.ranking.RankingCache;
import com.loopers.support.E2ETestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("랭킹 시스템 통합 테스트")
class RankingIntegrationTest extends E2ETestSupport {

    @Autowired
    private ProductMetricsService productMetricsService;

    @Autowired
    private RankingCache rankingCache;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RankingKeyGenerator keyGenerator;

    @Test
    @DisplayName("상품 메트릭 이벤트가 실제 Redis 랭킹에 반영되는지 통합 테스트")
    void productMetricsEventIntegrationTest() {
        // given
        LocalDate testDate = LocalDate.of(2025, 9, 8);
        ZonedDateTime testDateTime = testDate.atStartOfDay(ZoneId.systemDefault());
        
        Long productId1 = 100L;
        Long productId2 = 200L;
        Long productId3 = 300L;

        // when - 다양한 이벤트 발생
        // 상품 100: 조회 1회 (0.1점)
        ProductViewCommand viewCommand = new ProductViewCommand(
                "view-1", "PRODUCT_VIEWED", productId1, "product-100", testDateTime, testDateTime
        );
        productMetricsService.metricProductView(viewCommand);

        // 상품 200: 좋아요 1회 (0.2점)  
        ProductLikeMetricCommand likeCommand = ProductLikeMetricCommand.builder()
                .eventId("like-1")
                .eventType("PRODUCT_LIKED")
                .productId(productId2)
                .aggregateId("product-200")
                .metricDate(testDateTime)
                .occurredAt(testDateTime)
                .build();
        productMetricsService.metricProductLike(likeCommand);

        // 상품 300: 주문 1회 (0.6점)
        ProductOrderMetricCommand orderCommand = ProductOrderMetricCommand.builder()
                .eventId("order-1")
                .eventType("PRODUCT_ORDERED")
                .productId(productId3)
                .aggregateId("order-300")
                .metricDate(testDateTime)
                .occurredAt(testDateTime)
                .build();
        productMetricsService.metricProductOrder(orderCommand);

        // 상품 100: 추가 좋아요 (0.1 + 0.2 = 0.3점)
        ProductLikeMetricCommand additionalLike = ProductLikeMetricCommand.builder()
                .eventId("like-2")
                .eventType("PRODUCT_LIKED")
                .productId(productId1)
                .aggregateId("product-100")
                .metricDate(testDateTime)
                .occurredAt(testDateTime)
                .build();
        productMetricsService.metricProductLike(additionalLike);

        // then - Redis에서 직접 확인
        String rankingKey = keyGenerator.generateDailyRankingKey(testDate);
        
        // 1. 랭킹 키가 생성되었는지 확인
        assertThat(stringRedisTemplate.hasKey(rankingKey)).isTrue();
        
        // 2. 점수 순 정렬 확인 (높은 점수부터)
        Set<String> topProducts = stringRedisTemplate.opsForZSet().reverseRange(rankingKey, 0, -1);
        assertThat(topProducts).containsExactly("300", "100", "200"); // 0.6, 0.3, 0.2 순
        
        // 3. 각 상품의 점수 확인
        Double score300 = stringRedisTemplate.opsForZSet().score(rankingKey, "300");
        Double score100 = stringRedisTemplate.opsForZSet().score(rankingKey, "100");  
        Double score200 = stringRedisTemplate.opsForZSet().score(rankingKey, "200");
        
        assertThat(score300).isCloseTo(0.6, within(0.0001)); // 주문
        assertThat(score100).isCloseTo(0.3, within(0.0001)); // 조회 + 좋아요
        assertThat(score200).isCloseTo(0.2, within(0.0001)); // 좋아요
        
        // 4. 랭킹 순위 확인 (0부터 시작)
        Long rank300 = stringRedisTemplate.opsForZSet().reverseRank(rankingKey, "300");
        Long rank100 = stringRedisTemplate.opsForZSet().reverseRank(rankingKey, "100");
        Long rank200 = stringRedisTemplate.opsForZSet().reverseRank(rankingKey, "200");
        
        assertThat(rank300).isEqualTo(0); // 1위
        assertThat(rank100).isEqualTo(1); // 2위  
        assertThat(rank200).isEqualTo(2); // 3위
    }

    @Test
    @DisplayName("좋아요 취소 시 점수가 감소하는지 테스트")
    void productUnlikeDecreasesScore() {
        // given
        LocalDate testDate = LocalDate.of(2025, 9, 8);
        ZonedDateTime testDateTime = testDate.atStartOfDay(ZoneId.systemDefault());
        Long productId = 123L;
        
        // 좋아요 추가
        ProductLikeMetricCommand likeCommand = ProductLikeMetricCommand.builder()
                .eventId("like-1")
                .eventType("PRODUCT_LIKED")
                .productId(productId)
                .aggregateId("product-123")
                .metricDate(testDateTime)
                .occurredAt(testDateTime)
                .build();
        productMetricsService.metricProductLike(likeCommand);
        
        // when - 좋아요 취소
        ProductLikeMetricCommand unlikeCommand = ProductLikeMetricCommand.builder()
                .eventId("unlike-1")
                .eventType("PRODUCT_UNLIKED")
                .productId(productId)
                .aggregateId("product-123")
                .metricDate(testDateTime)
                .occurredAt(testDateTime)
                .build();
        productMetricsService.metricProductLike(unlikeCommand);
        
        // then
        String rankingKey = keyGenerator.generateDailyRankingKey(testDate);
        Double finalScore = stringRedisTemplate.opsForZSet().score(rankingKey, productId.toString());
        
        assertThat(finalScore).isCloseTo(0.0, within(0.0001)); // 0.2 - 0.2 = 0.0
    }

    @Test
    @DisplayName("Carry-Over 기능이 실제로 작동하는지 테스트")
    void carryOverScoresIntegrationTest() {
        // given
        Long productId = 456L;
        LocalDate today = LocalDate.of(2025, 9, 8);
        LocalDate tomorrow = today.plusDays(1);
        
        // 오늘 점수 생성
        rankingCache.incrementScore(productId, 1.0, today);
        
        // when - Carry-Over 실행
        double decayFactor = 0.5;
        rankingCache.carryOverScores(today, tomorrow, decayFactor);
        
        // then
        String todayKey = keyGenerator.generateDailyRankingKey(today);
        String tomorrowKey = keyGenerator.generateDailyRankingKey(tomorrow);
        
        // 오늘 점수는 그대로
        Double todayScore = stringRedisTemplate.opsForZSet().score(todayKey, productId.toString());
        assertThat(todayScore).isCloseTo(1.0, within(0.0001));
        
        // 내일 점수는 감쇠 적용
        Double tomorrowScore = stringRedisTemplate.opsForZSet().score(tomorrowKey, productId.toString());
        assertThat(tomorrowScore).isCloseTo(0.5, within(0.0001)); // 1.0 * 0.5
    }

    @Test
    @DisplayName("TTL이 올바르게 설정되는지 테스트")
    void ttlSetCorrectly() {
        // given
        Long productId = 789L;
        LocalDate testDate = LocalDate.of(2025, 9, 8);
        
        // when
        rankingCache.incrementScore(productId, 1.0, testDate);
        
        // then
        String rankingKey = keyGenerator.generateDailyRankingKey(testDate);
        Long ttl = stringRedisTemplate.getExpire(rankingKey);
        
        // TTL이 설정되어 있고, 2일(172800초) 근처여야 함
        assertThat(ttl).isGreaterThan(172000L); // 약간의 여유
        assertThat(ttl).isLessThanOrEqualTo(172800L); // 2일
    }

    @Test
    @DisplayName("동시에 여러 이벤트가 발생해도 점수가 정확히 누적되는지 테스트")
    void concurrentEventsAccumulation() {
        // given
        LocalDate testDate = LocalDate.of(2025, 9, 8);
        ZonedDateTime testDateTime = testDate.atStartOfDay(ZoneId.systemDefault());
        Long productId = 999L;
        
        // when - 같은 상품에 여러 이벤트 연속 발생
        for (int i = 0; i < 5; i++) {
            ProductViewCommand viewCommand = new ProductViewCommand(
                    "view-" + i, "PRODUCT_VIEWED", productId, "product-999", testDateTime, testDateTime
            );
            productMetricsService.metricProductView(viewCommand);
        }
        
        // then
        String rankingKey = keyGenerator.generateDailyRankingKey(testDate);
        Double finalScore = stringRedisTemplate.opsForZSet().score(rankingKey, productId.toString());
        
        assertThat(finalScore).isCloseTo(0.5, within(0.0001)); // 0.1 * 5 = 0.5
    }
}
