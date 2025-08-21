package com.loopers.scheduling.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 스케줄링 작업 메트릭스 수집 클래스
 * 
 *
 */
@Component
public class SchedulingMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> successCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCounters = new ConcurrentHashMap<>();

    public SchedulingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 스케줄링 작업 실행 시간 측정
     */
    public Timer.Sample startTimer(String taskName) {
        Timer timer = timers.computeIfAbsent(taskName, name ->
            Timer.builder("loopers.scheduling.execution.time")
                .description("Scheduling task execution time")
                .tag("task", name)
                .register(meterRegistry)
        );
        return Timer.start(meterRegistry);
    }

    /**
     * 실행 시간 기록 완료
     */
    public void stopTimer(Timer.Sample sample, String taskName) {
        Timer timer = timers.get(taskName);
        if (timer != null) {
            sample.stop(timer);
        }
    }

    /**
     * 성공 카운터 증가
     */
    public void incrementSuccess(String taskName) {
        Counter counter = successCounters.computeIfAbsent(taskName, name ->
            Counter.builder("loopers.scheduling.executions")
                .description("Scheduling task execution count")
                .tag("task", name)
                .tag("result", "success")
                .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 에러 카운터 증가
     */
    public void incrementError(String taskName, String errorType) {
        Counter counter = errorCounters.computeIfAbsent(taskName + ":" + errorType, key ->
            Counter.builder("loopers.scheduling.executions")
                .description("Scheduling task execution count")
                .tag("task", taskName)
                .tag("result", "error")
                .tag("error_type", errorType)
                .register(meterRegistry)
        );
        counter.increment();
    }
}
