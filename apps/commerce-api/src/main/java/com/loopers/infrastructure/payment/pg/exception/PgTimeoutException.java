package com.loopers.infrastructure.payment.pg.exception;

/**
 * PG 응답 시간 초과 (504 에러)
 * 네트워크 지연 또는 PG 처리 지연
 */
public class PgTimeoutException extends PgException {
    
    public PgTimeoutException(String message) {
        super(message);
    }
    
    public PgTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public boolean isRetryable() {
        // 타임아웃은 재시도 가능
        return true;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 타임아웃은 Circuit Breaker에 기록
        return true;
    }
}
