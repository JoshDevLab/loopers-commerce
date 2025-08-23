package com.loopers.scheduling.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 스케줄링 설정 클래스
 * 
 * @author loopers-team
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SchedulingProperties.class)
public class SchedulingConfig {

    private final SchedulingProperties schedulingProperties;

    public SchedulingConfig(SchedulingProperties schedulingProperties) {
        this.schedulingProperties = schedulingProperties;
    }

    /**
     * 기본 Task Scheduler 설정
     */
    @Bean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 스레드 풀 설정
        scheduler.setPoolSize(schedulingProperties.getPoolSize());
        scheduler.setThreadNamePrefix(schedulingProperties.getThreadNamePrefix());
        scheduler.setAwaitTerminationSeconds(schedulingProperties.getAwaitTerminationSeconds());
        scheduler.setWaitForTasksToCompleteOnShutdown(schedulingProperties.isWaitForTasksToCompleteOnShutdown());
        
        // 거부된 작업 처리 정책
        scheduler.setRejectedExecutionHandler(schedulingProperties.getRejectedExecutionHandler());
        
        scheduler.initialize();
        return scheduler;
    }
}
