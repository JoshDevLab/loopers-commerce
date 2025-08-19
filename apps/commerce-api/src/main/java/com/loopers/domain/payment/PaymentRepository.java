package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findById(Long paymentId);
}
