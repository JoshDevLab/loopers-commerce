package com.loopers.infrastructure.payment.pg.exception;

/**
 * 일반적인 PG 오류
 * 분류되지 않은 PG 관련 오류
 */
public class PgGeneralException extends PgException {
    
    public PgGeneralException(String message) {
        super(message);
    }
    
    public PgGeneralException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public boolean isRetryable() {
        // 일반적인 오류는 재시도하지 않음
        return false;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 일반적인 오류는 Circuit Breaker에 기록
        return true;
    }
}
