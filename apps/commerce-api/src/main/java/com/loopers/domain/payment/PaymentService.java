package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessorManager paymentProcessorManager;

    public ExternalPaymentResponse payment(PaymentCommand.Request paymentCommand) {
        Order order = orderRepository.findById(paymentCommand.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND));

        if (order.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreException(ErrorType.INVALID_PAID_AMOUNT, "결제금액은 0원 초과이어야 합니다");
        }

        PaymentProcessor paymentProcessor = paymentProcessorManager.getProcessor(paymentCommand.paymentType());
        ExternalPaymentRequest request = paymentProcessor.createRequest(paymentCommand, order.getPaidAmount());
        return paymentProcessor.payment(request);
    }

    @Transactional
    public Payment create(PaymentCommand.Request paymentCommand) {
        Order order = orderRepository.findById(paymentCommand.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND));

        Payment payment = Payment.create(
                paymentCommand.paymentType(),
                paymentCommand.cardType(),
                paymentCommand.cardNo(),
                Payment.PaymentStatus.PENDING,
                order.getPaidAmount(),
                order);
        paymentRepository.save(payment);
        return payment;
    }

    public boolean existsByOrderIdAndStatus(Long orderId, Payment.PaymentStatus status) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, status);
    }

    @Transactional
    public Payment updateSuccessStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.PAYMENT_NOT_FOUND));
        payment.success();
        return payment;
    }

    @Transactional
    public void updateTransactionId(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.PAYMENT_NOT_FOUND));
        payment.updateTransactionId(transactionId);
    }

    public Payment findByTransactionId(String transactionKey) {
        return paymentRepository.findByTransactionId(transactionKey)
                .orElseThrow(() -> new CoreException(ErrorType.PAYMENT_NOT_FOUND));
    }

    @Transactional
    public Payment updateFailedStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.PAYMENT_NOT_FOUND));
        payment.failed();
        return payment;
    }

    public ExternalPaymentResponse getTransactionIdFromExternal(String transactionKey) {
        Payment payment = paymentRepository.findByTransactionId(transactionKey)
                .orElseThrow(() -> new CoreException(ErrorType.PAYMENT_NOT_FOUND));
        PaymentProcessor processor = paymentProcessorManager.getProcessor(payment.getPaymentType());
        return processor.getByTransactionKey(transactionKey);
    }

    @Transactional(readOnly = true)
    public List<Payment> findPendingPayments() {
        return paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
    }

    public boolean hasSyncPaymentStatus(Payment payment) {
        try {
            PaymentProcessor paymentProcessor = paymentProcessorManager.getProcessor(payment.getPaymentType());
            ExternalPaymentResponse response = paymentProcessor.getByTransactionKey(payment.getTransactionId());

            if (response == null || !response.isSuccess()) {
                return false;
            }

            // PG 상태를 내부 상태로 변환
            Payment.PaymentStatus newStatus = convertPgStatusToPaymentStatus(response.getStatus());

            if (newStatus == Payment.PaymentStatus.PENDING) {
                return true;
            }

            int updatedRows = paymentRepository.updatePaymentStatus(
                    payment.getId(),
                    newStatus,
                    LocalDateTime.now()
            );

            if (updatedRows > 0) {

                // 결제 완료 시 후속 처리
                if (newStatus == Payment.PaymentStatus.SUCCESS) {
                    return true;
                } else return newStatus != Payment.PaymentStatus.FAILED;
            } else {
                return false;
            }

        } catch (Exception e) {
            log.error("결제 상태 동기화 중 오류 발생: paymentId={}", payment.getId(), e);
            return false;
        }
    }

    private Payment.PaymentStatus convertPgStatusToPaymentStatus(String pgStatus) {
        return switch (pgStatus.toUpperCase()) {
            case "SUCCESS", "COMPLETED", "PAID" -> Payment.PaymentStatus.SUCCESS;
            case "FAILED", "CANCELLED", "REJECTED" -> Payment.PaymentStatus.FAILED;
            case "CANCELED" -> Payment.PaymentStatus.CANCELED;
            case "PENDING", "PROCESSING", "IN_PROGRESS" -> Payment.PaymentStatus.PENDING;
            default -> {
                log.warn("알 수 없는 PG 상태: {}", pgStatus);
                yield Payment.PaymentStatus.PENDING;
            }
        };
    }
}
