package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingService {
    private final RankingCache rankingCache;
    private final ScoreCalculator scoreCalculator;

    public void updateProductLikeScore(Long productId, boolean isLikeEvent, LocalDate date) {
        double score = scoreCalculator.calculateLikeScore(isLikeEvent);
        rankingCache.incrementScore(productId, score, date);

        log.info("상품 좋아요 랭킹 점수 업데이트: productId={}, isLike={}, score={}, date={}",
                productId, isLikeEvent, score, date);
    }

    public void updateProductOrderScore(Long productId, LocalDate date) {
        double score = scoreCalculator.calculateOrderScore();
        rankingCache.incrementScore(productId, score, date);

        log.info("상품 주문 랭킹 점수 업데이트: productId={}, score={}, date={}",
                productId, score, date);
    }

    public void updateProductViewScore(Long productId, LocalDate date) {
        double score = scoreCalculator.calculateViewScore();
        rankingCache.incrementScore(productId, score, date);

        log.info("상품 조회 랭킹 점수 업데이트: productId={}, score={}, date={}",
                productId, score, date);
    }

    public void carryOverTodayScoresToTomorrow(double decayFactor) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        rankingCache.carryOverScores(today, tomorrow, decayFactor);
        log.info("오늘 점수를 내일로 Carry-Over 완료: today={}, tomorrow={}, factor={}",
                today, tomorrow, decayFactor);
    }

}
