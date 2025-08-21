package com.loopers.domain.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    boolean existsByOrderIdAndStatus(Long orderId, Payment.PaymentStatus status);

    Optional<Payment> findById(Long paymentId);

    Optional<Payment> findByPgTransactionId(String transactionKey);
    
    /**
     * 특정 상태의 결제 건들 조회
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    /**
     * 결제 상태 업데이트
     */
    int updatePaymentStatus(Long paymentId, Payment.PaymentStatus status, LocalDateTime updatedAt);
}
