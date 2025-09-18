package com.loopers.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class BatchScheduleConfig {
    
    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;
    
    /**
     * 매주 월요일 오전 2시에 주간 랭킹 집계
     * 전주 월요일~일요일 데이터를 집계
     */
    @Scheduled(cron = "0 0 2 * * MON", zone = "Asia/Seoul")
    public void runWeeklyRankingJob() {
        try {
            log.info("=== 주간 랭킹 배치 스케줄 실행 시작 ===");
            
            LocalDate lastMonday = LocalDate.now().minusWeeks(1)
                    .with(java.time.DayOfWeek.MONDAY);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("weekStartDate", lastMonday.toString())
                    .addString("jobTrigger", "SCHEDULED")
                    .toJobParameters();
            
            jobLauncher.run(weeklyRankingJob, jobParameters);
            log.info("=== 주간 랭킹 배치 스케줄 실행 완료 ===");
            
        } catch (Exception e) {
            log.error("주간 랭킹 배치 스케줄 실행 실패", e);
        }
    }
    
    /**
     * 매월 1일 오전 3시에 월간 랭킹 집계
     * 전월 데이터를 집계
     */
    @Scheduled(cron = "0 0 3 1 * *", zone = "Asia/Seoul")
    public void runMonthlyRankingJob() {
        try {
            log.info("=== 월간 랭킹 배치 스케줄 실행 시작 ===");
            
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("reportMonth", lastMonth.toString())
                    .addString("jobTrigger", "SCHEDULED")
                    .toJobParameters();
            
            jobLauncher.run(monthlyRankingJob, jobParameters);
            log.info("=== 월간 랭킹 배치 스케줄 실행 완료 ===");
            
        } catch (Exception e) {
            log.error("월간 랭킹 배치 스케줄 실행 실패", e);
        }
    }

}
