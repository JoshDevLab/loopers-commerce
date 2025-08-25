package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public boolean existsByOrderIdAndStatus(Long orderId, Payment.PaymentStatus status) {
        return paymentJpaRepository.existsByOrderIdAndStatus(orderId, status);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionKey) {
        return paymentJpaRepository.findByTransactionId(transactionKey);
    }

    @Override
    public List<Payment> findByStatus(Payment.PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status);
    }

    @Override
    public int updatePaymentStatus(Long paymentId, Payment.PaymentStatus status, LocalDateTime updatedAt) {
        return paymentJpaRepository.updatePaymentStatus(paymentId, status, updatedAt);
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }
}
