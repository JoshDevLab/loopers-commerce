package com.loopers.infrastructure.payment.pg.exception;

/**
 * PG 시뮬레이터의 400 Bad Request 오류
 * 잘못된 요청 형식, 필수 파라미터 누락 등
 */
public class PgBadRequestException extends PgException {
    
    private final String pgErrorCode;
    private final String pgMessage;
    
    public PgBadRequestException(String pgErrorCode, String pgMessage) {
        super(String.format("[%s] %s", pgErrorCode, pgMessage));
        this.pgErrorCode = pgErrorCode;
        this.pgMessage = pgMessage;
    }
    
    public PgBadRequestException(String pgErrorCode, String pgMessage, Throwable cause) {
        super(String.format("[%s] %s", pgErrorCode, pgMessage), cause);
        this.pgErrorCode = pgErrorCode;
        this.pgMessage = pgMessage;
    }
    
    public String getPgErrorCode() {
        return pgErrorCode;
    }
    
    public String getPgMessage() {
        return pgMessage;
    }
    
    @Override
    public boolean isRetryable() {
        // 클라이언트 오류는 재시도하지 않음
        return false;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 클라이언트 오류는 Circuit Breaker에 기록하지 않음
        return false;
    }
}
