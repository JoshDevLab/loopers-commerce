package com.loopers.domain.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingService 테스트")
class RankingServiceTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private RankingCache rankingCache;

    @Mock
    private ScoreCalculator scoreCalculator;

    private Long productId;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        productId = 123L;
        testDate = LocalDate.of(2025, 9, 8);
    }

    @Test
    @DisplayName("상품 좋아요 점수 업데이트")
    void updateProductLikeScore() {
        // given
        boolean isLikeEvent = true;
        double expectedScore = 0.2;
        
        given(scoreCalculator.calculateLikeScore(isLikeEvent))
                .willReturn(expectedScore);

        // when
        rankingService.updateProductLikeScore(productId, isLikeEvent, testDate);

        // then
        then(scoreCalculator).should().calculateLikeScore(isLikeEvent);
        then(rankingCache).should().incrementScore(productId, expectedScore, testDate);
    }

    @Test
    @DisplayName("상품 좋아요 취소 점수 업데이트")
    void updateProductUnlikeScore() {
        // given
        boolean isLikeEvent = false;
        double expectedScore = -0.2;
        
        given(scoreCalculator.calculateLikeScore(isLikeEvent))
                .willReturn(expectedScore);

        // when
        rankingService.updateProductLikeScore(productId, isLikeEvent, testDate);

        // then
        then(scoreCalculator).should().calculateLikeScore(isLikeEvent);
        then(rankingCache).should().incrementScore(productId, expectedScore, testDate);
    }

    @Test
    @DisplayName("상품 주문 점수 업데이트")
    void updateProductOrderScore() {
        // given
        double expectedScore = 0.6;
        
        given(scoreCalculator.calculateOrderScore())
                .willReturn(expectedScore);

        // when
        rankingService.updateProductOrderScore(productId, testDate);

        // then
        then(scoreCalculator).should().calculateOrderScore();
        then(rankingCache).should().incrementScore(productId, expectedScore, testDate);
    }

    @Test
    @DisplayName("상품 조회 점수 업데이트")
    void updateProductViewScore() {
        // given
        double expectedScore = 0.1;
        
        given(scoreCalculator.calculateViewScore())
                .willReturn(expectedScore);

        // when
        rankingService.updateProductViewScore(productId, testDate);

        // then
        then(scoreCalculator).should().calculateViewScore();
        then(rankingCache).should().incrementScore(productId, expectedScore, testDate);
    }

    @Test
    @DisplayName("오늘 점수를 내일로 Carry-Over")
    void carryOverTodayScoresToTomorrow() {
        // given
        double decayFactor = 0.3;
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // when
        rankingService.carryOverTodayScoresToTomorrow(decayFactor);

        // then
        then(rankingCache).should().carryOverScores(today, tomorrow, decayFactor);
    }

    @Test
    @DisplayName("여러 상품의 점수를 연속으로 업데이트")
    void updateMultipleProductScores() {
        // given
        Long productId1 = 100L;
        Long productId2 = 200L;
        double likeScore = 0.2;
        double viewScore = 0.1;
        double orderScore = 0.6;
        
        given(scoreCalculator.calculateLikeScore(true)).willReturn(likeScore);
        given(scoreCalculator.calculateViewScore()).willReturn(viewScore);
        given(scoreCalculator.calculateOrderScore()).willReturn(orderScore);

        // when
        rankingService.updateProductLikeScore(productId1, true, testDate);
        rankingService.updateProductViewScore(productId2, testDate);
        rankingService.updateProductOrderScore(productId1, testDate);

        // then
        then(rankingCache).should().incrementScore(productId1, likeScore, testDate);
        then(rankingCache).should().incrementScore(productId2, viewScore, testDate);
        then(rankingCache).should().incrementScore(productId1, orderScore, testDate);
    }

    @Test
    @DisplayName("동일 상품에 대한 여러 이벤트 처리")
    void updateSameProductMultipleEvents() {
        // given
        double likeScore = 0.2;
        double viewScore = 0.1;
        double orderScore = 0.6;
        
        given(scoreCalculator.calculateLikeScore(true)).willReturn(likeScore);
        given(scoreCalculator.calculateViewScore()).willReturn(viewScore);
        given(scoreCalculator.calculateOrderScore()).willReturn(orderScore);

        // when - 같은 상품에 여러 이벤트 발생
        rankingService.updateProductViewScore(productId, testDate);
        rankingService.updateProductLikeScore(productId, true, testDate);
        rankingService.updateProductOrderScore(productId, testDate);

        // then - 모든 점수가 누적되어야 함
        then(rankingCache).should().incrementScore(productId, viewScore, testDate);
        then(rankingCache).should().incrementScore(productId, likeScore, testDate);
        then(rankingCache).should().incrementScore(productId, orderScore, testDate);
    }
}
