package com.loopers.interfaces.api.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {
    
    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;
    
    @PostMapping("/weekly-ranking")
    public Map<String, Object> runWeeklyRankingJob(
            @RequestParam(required = false) String weekStartDate) {
        try {
            log.info("수동 주간 랭킹 배치 실행 요청 - weekStartDate: {}", weekStartDate);
            
            JobParametersBuilder builder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("jobTrigger", "MANUAL");
                    
            if (weekStartDate != null) {
                builder.addString("weekStartDate", weekStartDate);
            }
            
            JobParameters jobParameters = builder.toJobParameters();
            jobLauncher.run(weeklyRankingJob, jobParameters);
            
            return Map.of(
                "status", "SUCCESS",
                "message", "주간 랭킹 배치 실행 완료",
                "weekStartDate", weekStartDate != null ? weekStartDate : "자동 계산"
            );
        } catch (Exception e) {
            log.error("주간 랭킹 배치 실행 실패", e);
            return Map.of(
                "status", "FAILED",
                "message", "주간 랭킹 배치 실행 실패: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/monthly-ranking")
    public Map<String, Object> runMonthlyRankingJob(
            @RequestParam(required = false) String reportMonth) {
        try {
            log.info("수동 월간 랭킹 배치 실행 요청 - reportMonth: {}", reportMonth);
            
            JobParametersBuilder builder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("jobTrigger", "MANUAL");
                    
            if (reportMonth != null) {
                builder.addString("reportMonth", reportMonth);
            }
            
            JobParameters jobParameters = builder.toJobParameters();
            jobLauncher.run(monthlyRankingJob, jobParameters);
            
            return Map.of(
                "status", "SUCCESS", 
                "message", "월간 랭킹 배치 실행 완료",
                "reportMonth", reportMonth != null ? reportMonth : "자동 계산"
            );
        } catch (Exception e) {
            log.error("월간 랭킹 배치 실행 실패", e);
            return Map.of(
                "status", "FAILED",
                "message", "월간 랭킹 배치 실행 실패: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/status")
    public Map<String, Object> getBatchStatus() {
        return Map.of(
            "applicationName", "commerce-batch",
            "schedulingEnabled", true,
            "currentTime", LocalDate.now().toString(),
            "nextWeeklyRun", "매주 월요일 오전 2시",
            "nextMonthlyRun", "매월 1일 오전 3시"
        );
    }
}
