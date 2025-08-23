package com.loopers.infrastructure.payment.pg.exception;

/**
 * PG 시뮬레이터의 비즈니스 로직 오류
 * - 한도 초과 (LIMIT_EXCEEDED)
 * - 잘못된 카드 (INVALID_CARD)
 * - 기타 결제 처리 실패
 * 
 * HTTP 200으로 응답되지만 meta.result가 FAIL이고 
 * data.status가 FAILED인 경우
 */
public class PgBusinessException extends PgException {
    
    private final String transactionKey;
    private final String transactionStatus; // FAILED
    private final String reason; // PG 시뮬레이터의 실패 사유
    
    public PgBusinessException(String transactionKey, String transactionStatus, String reason) {
        super(String.format("PG 비즈니스 로직 실패 - TransactionKey: %s, Status: %s, Reason: %s", 
                transactionKey, transactionStatus, reason));
        this.transactionKey = transactionKey;
        this.transactionStatus = transactionStatus;
        this.reason = reason;
    }
    
    public PgBusinessException(String transactionKey, String transactionStatus, String reason, Throwable cause) {
        super(String.format("PG 비즈니스 로직 실패 - TransactionKey: %s, Status: %s, Reason: %s", 
                transactionKey, transactionStatus, reason), cause);
        this.transactionKey = transactionKey;
        this.transactionStatus = transactionStatus;
        this.reason = reason;
    }
    
    public String getTransactionKey() {
        return transactionKey;
    }
    
    public String getTransactionStatus() {
        return transactionStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    /**
     * PG 시뮬레이터의 실패 사유를 기반으로 에러 타입 추론
     */
    public String getInferredErrorType() {
        if (reason == null) {
            return "UNKNOWN_ERROR";
        }
        
        String reasonLower = reason.toLowerCase();
        if (reasonLower.contains("한도초과")) {
            return "LIMIT_EXCEEDED";
        }
        if (reasonLower.contains("잘못된 카드")) {
            return "INVALID_CARD";
        }
        if (reasonLower.contains("유효기간")) {
            return "EXPIRED_CARD";
        }
        if (reasonLower.contains("잔액")) {
            return "INSUFFICIENT_FUNDS";
        }
        if (reasonLower.contains("차단") || reasonLower.contains("정지")) {
            return "CARD_BLOCKED";
        }
        
        return "PAYMENT_FAILED";
    }
    
    @Override
    public boolean isRetryable() {
        // 비즈니스 로직 오류는 재시도하지 않음
        return false;
    }
    
    @Override
    public boolean shouldRecordAsFailure() {
        // 비즈니스 오류는 Circuit Breaker에 기록하지 않음 (정상적인 PG 응답)
        return false;
    }
}
