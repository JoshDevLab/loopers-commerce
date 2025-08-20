package com.loopers.infrastructure.payment.pg.exception;

/**
 * PG 관련 예외의 최상위 클래스
 */
public abstract class PgException extends RuntimeException {
    
    public PgException(String message) {
        super(message);
    }
    
    public PgException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 재시도 가능 여부
     * @return true if retryable, false otherwise
     */
    public abstract boolean isRetryable();
    
    /**
     * Circuit Breaker에 실패로 기록할지 여부
     * @return true if should record as failure, false otherwise
     */
    public abstract boolean shouldRecordAsFailure();
}
