package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCache;
import com.loopers.domain.ranking.RankingItem;
import com.loopers.support.RedisZSetOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingCacheImpl implements RankingCache {

    private final RedisZSetOperations redisZSetOperations;
    private final RankingKeyGenerator keyGenerator;

    @Override
    public List<RankingItem> getRankings(LocalDate date, long offset, long count) {
        String key = keyGenerator.generateDailyRankingKey(date);

        Set<ZSetOperations.TypedTuple<String>> rankings =
                redisZSetOperations.reverseRangeWithScores(key, offset, offset + count - 1);

        AtomicLong rank = new AtomicLong(offset + 1);

        return rankings.stream()
                .map(tuple -> new RankingItem(
                        Long.parseLong(tuple.getValue()),
                        tuple.getScore(),
                        rank.getAndIncrement()
                ))
                .toList();
    }

    @Override
    public Long getProductRank(Long productId, LocalDate date) {
        String key = keyGenerator.generateDailyRankingKey(date);

        Long reverseRank = redisZSetOperations.reverseRank(key, productId.toString());

        return reverseRank != null ? reverseRank + 1 : null;
    }

    @Override
    public long getTotalCount(LocalDate date) {
        String key = keyGenerator.generateDailyRankingKey(date);

        Long count = redisZSetOperations.zCard(key);

        return count != null ? count : 0L;
    }
}
