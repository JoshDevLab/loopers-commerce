package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingItem;
import com.loopers.support.RedisZSetOperations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingCacheImplTest {

    @Mock
    RedisZSetOperations redisZSetOperations;

    @Mock
    RankingKeyGenerator keyGenerator;

    @InjectMocks
    RankingCacheImpl sut;

    @DisplayName("랭킹 조회 시 Redis ZSet에서 역순으로 데이터를 가져온다")
    @Test
    void getRankings() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        long offset = 0;
        long count = 2;
        String key = "ranking:all:20250115";
        
        when(keyGenerator.generateDailyRankingKey(date)).thenReturn(key);
        
        ZSetOperations.TypedTuple<String> tuple1 = mock(ZSetOperations.TypedTuple.class);
        when(tuple1.getValue()).thenReturn("123");
        when(tuple1.getScore()).thenReturn(100.0);
        
        ZSetOperations.TypedTuple<String> tuple2 = mock(ZSetOperations.TypedTuple.class);
        when(tuple2.getValue()).thenReturn("456");
        when(tuple2.getScore()).thenReturn(90.0);
        
        Set<ZSetOperations.TypedTuple<String>> tuples = Set.of(tuple1, tuple2);
        when(redisZSetOperations.reverseRangeWithScores(key, offset, offset + count - 1))
                .thenReturn(tuples);

        // Act
        List<RankingItem> result = sut.getRankings(date, offset, count);

        // Assert
        assertThat(result).hasSize(2);
        verify(keyGenerator, times(1)).generateDailyRankingKey(date);
        verify(redisZSetOperations, times(1)).reverseRangeWithScores(key, 0, 1);
        
        // 순위는 offset + 1부터 시작
        assertThat(result.stream().mapToLong(RankingItem::getRank).toArray())
                .containsExactly(1L, 2L);
    }

    @DisplayName("상품 순위 조회 시 Redis에서 역순 순위를 가져와 1을 더한다")
    @Test
    void getProductRank() {
        // Arrange
        Long productId = 123L;
        LocalDate date = LocalDate.of(2025, 1, 15);
        String key = "ranking:all:20250115";
        
        when(keyGenerator.generateDailyRankingKey(date)).thenReturn(key);
        when(redisZSetOperations.reverseRank(key, productId.toString())).thenReturn(4L); // 0-based

        // Act
        Long result = sut.getProductRank(productId, date);

        // Assert
        assertThat(result).isEqualTo(5L); // 1-based로 변환
        verify(keyGenerator, times(1)).generateDailyRankingKey(date);
        verify(redisZSetOperations, times(1)).reverseRank(key, "123");
    }

    @DisplayName("상품이 랭킹에 없는 경우 null을 반환한다")
    @Test
    void getProductRank_notFound() {
        // Arrange
        Long productId = 999L;
        LocalDate date = LocalDate.of(2025, 1, 15);
        String key = "ranking:all:20250115";
        
        when(keyGenerator.generateDailyRankingKey(date)).thenReturn(key);
        when(redisZSetOperations.reverseRank(key, productId.toString())).thenReturn(null);

        // Act
        Long result = sut.getProductRank(productId, date);

        // Assert
        assertThat(result).isNull();
        verify(keyGenerator, times(1)).generateDailyRankingKey(date);
        verify(redisZSetOperations, times(1)).reverseRank(key, "999");
    }

    @DisplayName("전체 랭킹 개수 조회 시 Redis ZCard를 사용한다")
    @Test
    void getTotalCount() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        String key = "ranking:all:20250115";
        
        when(keyGenerator.generateDailyRankingKey(date)).thenReturn(key);
        when(redisZSetOperations.zCard(key)).thenReturn(100L);

        // Act
        long result = sut.getTotalCount(date);

        // Assert
        assertThat(result).isEqualTo(100L);
        verify(keyGenerator, times(1)).generateDailyRankingKey(date);
        verify(redisZSetOperations, times(1)).zCard(key);
    }

    @DisplayName("전체 랭킹 개수가 null인 경우 0을 반환한다")
    @Test
    void getTotalCount_null() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        String key = "ranking:all:20250115";
        
        when(keyGenerator.generateDailyRankingKey(date)).thenReturn(key);
        when(redisZSetOperations.zCard(key)).thenReturn(null);

        // Act
        long result = sut.getTotalCount(date);

        // Assert
        assertThat(result).isEqualTo(0L);
        verify(keyGenerator, times(1)).generateDailyRankingKey(date);
        verify(redisZSetOperations, times(1)).zCard(key);
    }

    @DisplayName("빈 랭킹 데이터 조회 시 빈 리스트를 반환한다")
    @Test
    void getRankings_empty() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        long offset = 0;
        long count = 10;
        String key = "ranking:all:20250115";
        
        when(keyGenerator.generateDailyRankingKey(date)).thenReturn(key);
        when(redisZSetOperations.reverseRangeWithScores(key, offset, offset + count - 1))
                .thenReturn(Set.of());

        // Act
        List<RankingItem> result = sut.getRankings(date, offset, count);

        // Assert
        assertThat(result).isEmpty();
        verify(keyGenerator, times(1)).generateDailyRankingKey(date);
        verify(redisZSetOperations, times(1)).reverseRangeWithScores(key, 0, 9);
    }
}
