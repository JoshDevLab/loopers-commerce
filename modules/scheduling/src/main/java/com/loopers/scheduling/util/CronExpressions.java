package com.loopers.scheduling.util;

/**
 * 자주 사용되는 Cron 표현식 상수 클래스
 * 
 *
 */
public final class CronExpressions {

    private CronExpressions() {
        // 유틸리티 클래스
    }

    // === 매일 실행 ===
    
    /** 매일 자정 (00:00) */
    public static final String DAILY_MIDNIGHT = "0 0 0 * * *";
    
    /** 매일 오전 6시 */
    public static final String DAILY_6AM = "0 0 6 * * *";
    
    /** 매일 오후 6시 */
    public static final String DAILY_6PM = "0 0 18 * * *";

    // === 매시간 실행 ===
    
    /** 매시간 정각 */
    public static final String HOURLY = "0 0 * * * *";
    
    /** 매시간 30분 */
    public static final String HOURLY_30MIN = "0 30 * * * *";

    // === 매분 실행 ===
    
    /** 매분 정각 */
    public static final String EVERY_MINUTE = "0 * * * * *";
    
    /** 5분마다 */
    public static final String EVERY_5_MINUTES = "0 */5 * * * *";
    
    /** 10분마다 */
    public static final String EVERY_10_MINUTES = "0 */10 * * * *";
    
    /** 30분마다 */
    public static final String EVERY_30_MINUTES = "0 */30 * * * *";

    // === 주간 실행 ===
    
    /** 매주 월요일 자정 */
    public static final String WEEKLY_MONDAY = "0 0 0 * * MON";
    
    /** 매주 일요일 자정 */
    public static final String WEEKLY_SUNDAY = "0 0 0 * * SUN";

    // === 월간 실행 ===
    
    /** 매월 1일 자정 */
    public static final String MONTHLY_FIRST_DAY = "0 0 0 1 * *";
    
    /** 매월 말일 자정 */
    public static final String MONTHLY_LAST_DAY = "0 0 0 L * *";

    // === 비즈니스 시간 ===
    
    /** 평일 오전 9시 (월~금) */
    public static final String WEEKDAYS_9AM = "0 0 9 * * MON-FRI";
    
    /** 평일 오후 6시 (월~금) */
    public static final String WEEKDAYS_6PM = "0 0 18 * * MON-FRI";
}
