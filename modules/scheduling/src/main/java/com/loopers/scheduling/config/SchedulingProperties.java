package com.loopers.scheduling.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 스케줄링 설정 Properties
 * 
 * @author loopers-team
 */
@ConfigurationProperties(prefix = "loopers.scheduling")
@Validated
public class SchedulingProperties {

    /**
     * 스케줄러 스레드 풀 크기
     */
    @Min(1)
    private int poolSize = 10;

    /**
     * 스레드 이름 접두사
     */
    @NotBlank
    private String threadNamePrefix = "loopers-scheduler-";

    /**
     * 종료 시 대기 시간 (초)
     */
    @Min(0)
    private int awaitTerminationSeconds = 30;

    /**
     * 종료 시 작업 완료까지 대기 여부
     */
    private boolean waitForTasksToCompleteOnShutdown = true;

    /**
     * 거부된 작업 처리 정책
     */
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    // Getters and Setters
    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public int getAwaitTerminationSeconds() {
        return awaitTerminationSeconds;
    }

    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    public boolean isWaitForTasksToCompleteOnShutdown() {
        return waitForTasksToCompleteOnShutdown;
    }

    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }
}
