package com.loopers.application.payment;

/**
 * 콜백 데이터 동기화 실패 예외
 */
public class DataSyncException extends RuntimeException {
    public DataSyncException(String message) {
        super(message);
    }
    
    public DataSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
