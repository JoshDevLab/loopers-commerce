package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessorManager paymentProcessorManager;

    public void payment(PaymentCommand.Request paymentCommand) {
        Order order = orderRepository.findById(paymentCommand.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND));

        if (order.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreException(ErrorType.INVALID_PAID_AMOUNT, "결제금액은 0원 초과이어야 합니다");
        }

        PaymentProcessor paymentProcessor = paymentProcessorManager.getProcessor(paymentCommand.paymentType());
        ExternalPaymentRequest request = paymentProcessor.createRequest(paymentCommand, order.getPaidAmount());
        paymentProcessor.payment(request);
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
}
