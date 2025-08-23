package com.loopers.infrastructure.payment.pg.exception;

/**
 * PG 서비스 이용 불가 (503 에러)
 * 일시적인 서비스 장애
 */
public class PgServiceUnavailableException extends PgException {
    
    public PgServiceUnavailableException(String message) {
        super(message);
    }
    
    public PgServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public boolean isRetryable() {
        // 서비스 이용 불가는 재시도 가능
        return true;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 서비스 장애는 Circuit Breaker에 기록
        return true;
    }
}
