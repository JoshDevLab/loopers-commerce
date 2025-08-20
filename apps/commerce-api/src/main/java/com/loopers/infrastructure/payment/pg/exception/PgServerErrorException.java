package com.loopers.infrastructure.payment.pg.exception;

import lombok.Getter;

/**
 * PG 시뮬레이터의 500 Internal Server Error
 * 서버 불안정, 내부 처리 오류 등
 */
@Getter
public class PgServerErrorException extends PgException {
    
    private final String pgErrorCode;
    private final String pgMessage;
    
    public PgServerErrorException(String pgErrorCode, String pgMessage) {
        super(String.format("[%s] %s", pgErrorCode, pgMessage));
        this.pgErrorCode = pgErrorCode;
        this.pgMessage = pgMessage;
    }
    
    public PgServerErrorException(String pgErrorCode, String pgMessage, Throwable cause) {
        super(String.format("[%s] %s", pgErrorCode, pgMessage), cause);
        this.pgErrorCode = pgErrorCode;
        this.pgMessage = pgMessage;
    }
    
    // 기존 생성자 유지 (하위 호환성)
    public PgServerErrorException(String message) {
        this("INTERNAL_SERVER_ERROR", message);
    }
    
    public PgServerErrorException(String message, Throwable cause) {
        this("INTERNAL_SERVER_ERROR", message, cause);
    }
    
    @Override
    public boolean isRetryable() {
        // 서버 오류는 재시도 가능
        return true;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 서버 오류는 Circuit Breaker에 기록
        return true;
    }
}
