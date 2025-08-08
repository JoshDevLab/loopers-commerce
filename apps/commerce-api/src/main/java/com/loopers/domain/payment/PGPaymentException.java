package com.loopers.domain.payment;

public class PGPaymentException extends RuntimeException {
    public PGPaymentException(String message) {
        super(message);
    }

    public PGPaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public PGPaymentException(Throwable cause) {
        super(cause);
    }
}
