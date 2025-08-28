package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrderIdAndStatus(Long order_id, PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionKey);
    
    /**
     * 특정 상태의 결제 건들 조회
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * 결제 상태 업데이트
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.updatedAt = :updatedAt WHERE p.id = :paymentId")
    int updatePaymentStatus(@Param("paymentId") Long paymentId, 
                           @Param("status") PaymentStatus status, 
                           @Param("updatedAt") LocalDateTime updatedAt);

    List<Payment> findByOrderId(Long orderId);
}
