package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    boolean existsByOrderIdAndStatus(Long orderId, Payment.PaymentStatus status);

    Optional<Payment> findById(Long paymentId);

    Optional<Payment> findByPgTransactionId(String transactionKey);
}
