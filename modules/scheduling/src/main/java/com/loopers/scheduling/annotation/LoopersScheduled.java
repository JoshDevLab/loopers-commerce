package com.loopers.scheduling.annotation;

import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Loopers 커스텀 스케줄링 어노테이션
 * 
 * Spring의 @Scheduled를 확장하여 프로젝트 특화 기능을 제공합니다.
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scheduled
public @interface LoopersScheduled {

    /**
     * 스케줄 작업 이름 (모니터링용)
     */
    String name() default "";

    /**
     * 작업 설명
     */
    String description() default "";

    /**
     * Cron 표현식
     * 예: "0 0 * * * *" (매시간)
     */
    String cron() default "";

    /**
     * Cron 표현식의 시간대
     */
    String zone() default "";

    /**
     * 고정 지연 시간 (밀리초)
     * 이전 실행 완료 후 다음 실행까지의 지연
     */
    long fixedDelay() default -1;

    /**
     * 고정 지연 시간 문자열 (SpEL 지원)
     */
    String fixedDelayString() default "";

    /**
     * 고정 주기 (밀리초)
     * 이전 실행 시작 시점부터 다음 실행까지의 간격
     */
    long fixedRate() default -1;

    /**
     * 고정 주기 문자열 (SpEL 지원)
     */
    String fixedRateString() default "";

    /**
     * 초기 지연 시간 (밀리초)
     */
    long initialDelay() default -1;

    /**
     * 초기 지연 시간 문자열 (SpEL 지원)
     */
    String initialDelayString() default "";

    /**
     * 환경별 실행 여부 (프로파일)
     * 예: {"local", "dev"}
     */
    String[] profiles() default {};

    /**
     * 실행 가능 여부 조건 (SpEL)
     * 예: "#{@environment.getProperty('scheduling.enabled', Boolean.class, true)}"
     */
    String condition() default "";
}
