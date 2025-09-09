package com.loopers.support.fixture.ranking;

import com.loopers.domain.ranking.RankingItem;
import com.loopers.support.RedisZSetOperations;

import java.time.LocalDate;
import java.util.List;

/**
 * 랭킹 테스트를 위한 픽스처 클래스
 */
public class RankingFixture {

    /**
     * 랭킹 아이템 생성
     */
    public static RankingItem createRankingItem(Long productId, Double score, Long rank) {
        return new RankingItem(productId, score, rank);
    }

    /**
     * 여러 랭킹 아이템 생성
     */
    public static List<RankingItem> createRankingItems() {
        return List.of(
                createRankingItem(1L, 100.0, 1L),
                createRankingItem(2L, 90.0, 2L),
                createRankingItem(3L, 80.0, 3L),
                createRankingItem(4L, 70.0, 4L),
                createRankingItem(5L, 60.0, 5L)
        );
    }

    /**
     * Redis에 랭킹 데이터 설정
     */
    public static void setupRankingData(RedisZSetOperations redisZSetOperations, 
                                       LocalDate date, 
                                       List<Long> productIds, 
                                       List<Double> scores) {
        if (productIds.size() != scores.size()) {
            throw new IllegalArgumentException("ProductIds and scores must have same size");
        }

        String rankingKey = "ranking:all:" + date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        for (int i = 0; i < productIds.size(); i++) {
            redisZSetOperations.add(rankingKey, productIds.get(i).toString(), scores.get(i));
        }
    }

    /**
     * 기본 랭킹 데이터 설정 (5개 상품)
     */
    public static void setupDefaultRankingData(RedisZSetOperations redisZSetOperations, 
                                              LocalDate date,
                                              List<Long> productIds) {
        List<Double> scores = List.of(100.0, 90.0, 80.0, 70.0, 60.0);
        
        // productIds가 5개보다 적으면 그만큼만 설정
        int size = Math.min(productIds.size(), scores.size());
        setupRankingData(
                redisZSetOperations, 
                date, 
                productIds.subList(0, size), 
                scores.subList(0, size)
        );
    }

    /**
     * 높은 점수부터 낮은 점수까지 설정
     */
    public static void setupRankingDataDescending(RedisZSetOperations redisZSetOperations,
                                                 LocalDate date,
                                                 List<Long> productIds,
                                                 double startScore,
                                                 double step) {
        String rankingKey = "ranking:all:" + date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        for (int i = 0; i < productIds.size(); i++) {
            double score = startScore - (i * step);
            redisZSetOperations.add(rankingKey, productIds.get(i).toString(), score);
        }
    }

    /**
     * 단일 상품 랭킹 설정
     */
    public static void setupSingleProductRanking(RedisZSetOperations redisZSetOperations,
                                                LocalDate date,
                                                Long productId,
                                                Double score) {
        String rankingKey = "ranking:all:" + date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.add(rankingKey, productId.toString(), score);
    }

    /**
     * 랭킹 데이터 클리어
     */
    public static void clearRankingData(RedisZSetOperations redisZSetOperations, LocalDate date) {
        String rankingKey = "ranking:all:" + date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.delete(rankingKey);
    }
}
