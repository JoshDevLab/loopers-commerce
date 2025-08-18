package com.loopers.feign.config.exception;

/**
 * Feign 서버 예외 (5xx 에러)
 */
public class FeignServerException extends RuntimeException {
    
    private final int status;

    public FeignServerException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
