package com.loopers.infrastructure.scheduling;

import com.loopers.domain.ranking.RankingService;
import com.loopers.support.redis.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingColdStartScheduler {
    
    private final RankingService rankingService;
    private final RedisDistributedLock distributedLock;
    
    @Value("${scheduling.tasks.ranking-carry-over.decay-factor:0.3}")
    private double decayFactor;
    
    private static final String RANKING_CARRY_OVER_LOCK_KEY = "ranking:carry-over:lock";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(10);
    
    @Scheduled(cron = "0 50 23 * * *") // 콜드 스타트 완화를 위한 점수 Carry-Over 23시 50분 내일 랭킹판을 미리 생성
    @ConditionalOnProperty(name = "scheduling.tasks.ranking-carry-over.enabled", havingValue = "true", matchIfMissing = true)
    public void carryOverRankingScores() {
        log.info("랭킹 점수 Carry-Over 스케줄러 시작");
        
        distributedLock.executeWithLock(
            RANKING_CARRY_OVER_LOCK_KEY, 
            LOCK_TIMEOUT, 
            this::doCarryOverScores
        );
        
        log.info("랭킹 점수 Carry-Over 스케줄러 완료");
    }
    
    private void doCarryOverScores() {
        try {
            long startTime = System.currentTimeMillis();
            
            // 오늘 점수를 내일로 감쇠 적용하여 복사 (콜드 스타트 방지)
            rankingService.carryOverTodayScoresToTomorrow(decayFactor);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("랭킹 점수 Carry-Over 완료 - 소요시간: {}ms, 감쇠인수: {}", duration, decayFactor);
            
        } catch (Exception e) {
            log.error("랭킹 점수 Carry-Over 실행 중 오류 발생", e);
        }
    }
    
}
