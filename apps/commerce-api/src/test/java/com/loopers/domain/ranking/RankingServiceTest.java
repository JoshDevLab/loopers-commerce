package com.loopers.domain.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    RankingCache rankingCache;

    @InjectMocks
    RankingService sut;

    @DisplayName("랭킹 조회 시 캐시에서 데이터를 가져온다")
    @Test
    void getRankings() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        long offset = 0;
        long count = 10;
        
        List<RankingItem> expectedItems = List.of(
                new RankingItem(1L, 100.0, 1L),
                new RankingItem(2L, 90.0, 2L)
        );
        
        when(rankingCache.getRankings(date, offset, count)).thenReturn(expectedItems);

        // Act
        List<RankingItem> result = sut.getRankings(date, offset, count);

        // Assert
        assertThat(result).isSameAs(expectedItems);
        verify(rankingCache, times(1)).getRankings(date, offset, count);
    }

    @DisplayName("상품 순위 조회 시 캐시에서 데이터를 가져온다")
    @Test
    void getProductRank() {
        // Arrange
        Long productId = 123L;
        LocalDate date = LocalDate.of(2025, 1, 15);
        Long expectedRank = 5L;
        
        when(rankingCache.getProductRank(productId, date)).thenReturn(expectedRank);

        // Act
        Long result = sut.getProductRank(productId, date);

        // Assert
        assertThat(result).isEqualTo(expectedRank);
        verify(rankingCache, times(1)).getProductRank(productId, date);
    }

    @DisplayName("상품 순위가 없는 경우 null을 반환한다")
    @Test
    void getProductRank_notFound() {
        // Arrange
        Long productId = 999L;
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        when(rankingCache.getProductRank(productId, date)).thenReturn(null);

        // Act
        Long result = sut.getProductRank(productId, date);

        // Assert
        assertThat(result).isNull();
        verify(rankingCache, times(1)).getProductRank(productId, date);
    }

    @DisplayName("전체 랭킹 개수 조회 시 캐시에서 데이터를 가져온다")
    @Test
    void getTotalCount() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        long expectedCount = 100L;
        
        when(rankingCache.getTotalCount(date)).thenReturn(expectedCount);

        // Act
        long result = sut.getTotalCount(date);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
        verify(rankingCache, times(1)).getTotalCount(date);
    }

    @DisplayName("전체 랭킹 개수가 0인 경우")
    @Test
    void getTotalCount_zero() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        when(rankingCache.getTotalCount(date)).thenReturn(0L);

        // Act
        long result = sut.getTotalCount(date);

        // Assert
        assertThat(result).isEqualTo(0L);
        verify(rankingCache, times(1)).getTotalCount(date);
    }
}
