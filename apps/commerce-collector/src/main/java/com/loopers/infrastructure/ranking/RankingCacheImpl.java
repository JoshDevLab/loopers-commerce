package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCache;
import com.loopers.support.RedisZSetOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingCacheImpl implements RankingCache {
    private final RedisZSetOperations redisZSetOperations;
    private final RankingKeyGenerator keyGenerator;

    private static final Duration TTL = Duration.ofDays(2);

    @Override
    public void incrementScore(Long productId, double score, LocalDate date) {
        String key = keyGenerator.generateDailyRankingKey(date);

        redisZSetOperations.incrementScore(key, productId.toString(), score);
        redisZSetOperations.expire(key, TTL);
    }

    @Override
    public void carryOverScores(LocalDate fromDate, LocalDate toDate, double decayFactor) {
        String fromKey = keyGenerator.generateDailyRankingKey(fromDate);
        String toKey = keyGenerator.generateDailyRankingKey(toDate);

        Set<ZSetOperations.TypedTuple<String>> previousScores =
                redisZSetOperations.reverseRangeWithScores(fromKey, 0, -1);

        previousScores.forEach(tuple -> {
            String productIdStr = tuple.getValue();
            Double score = tuple.getScore();

            if (productIdStr != null && score != null) {
                double newScore = score * decayFactor;
                redisZSetOperations.add(toKey, productIdStr, newScore);
            }
        });

        if (!previousScores.isEmpty()) {
            redisZSetOperations.expire(toKey, TTL);
        }
    }

}
