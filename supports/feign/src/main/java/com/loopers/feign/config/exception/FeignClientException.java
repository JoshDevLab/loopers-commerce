package com.loopers.feign.config.exception;

/**
 * Feign 클라이언트 예외 (4xx 에러)
 */
public class FeignClientException extends RuntimeException {
    
    private final int status;

    public FeignClientException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
