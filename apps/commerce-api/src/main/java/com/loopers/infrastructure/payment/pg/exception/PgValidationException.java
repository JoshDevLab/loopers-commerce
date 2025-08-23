package com.loopers.infrastructure.payment.pg.exception;

/**
 * PG 시뮬레이터의 요청 검증 오류 (400 에러)
 * - 주문 ID 형식 오류
 * - 카드번호 형식 오류
 * - 결제금액 오류
 * - 콜백 URL 형식 오류
 */
public class PgValidationException extends PgException {
    
    private final String validationField;
    
    public PgValidationException(String validationField, String message) {
        super(message);
        this.validationField = validationField;
    }
    
    public PgValidationException(String validationField, String message, Throwable cause) {
        super(message, cause);
        this.validationField = validationField;
    }
    
    public String getValidationField() {
        return validationField;
    }
    
    @Override
    public boolean isRetryable() {
        // 검증 오류는 재시도해도 동일한 결과
        return false;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 클라이언트 오류이므로 Circuit Breaker에 기록하지 않음
        return false;
    }
}
