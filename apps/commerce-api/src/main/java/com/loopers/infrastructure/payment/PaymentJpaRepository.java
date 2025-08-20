package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrderIdAndStatus(Long order_id, Payment.PaymentStatus status);
    Optional<Payment> findByPgTransactionId(String transactionKey);
}
