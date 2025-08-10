package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        if (order.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
            PaymentProcessor paymentProcessor = paymentProcessorManager.getProcessor(paymentCommand.paymentType());
            if (!paymentProcessor.payment(order.getPaidAmount())) {
                throw new PGPaymentException("PG사 응답 오류");
            }
        }
    }

    public Payment create(PaymentCommand.Request paymentCommand) {
        Order order = orderRepository.findById(paymentCommand.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND));

        Payment payment = Payment.create(paymentCommand.paymentType(), Payment.PaymentStatus.SUCCESS, order.getPaidAmount(), order);
        paymentRepository.save(payment);
        return payment;
    }

    public boolean existsByOrderId(Long orderId) {
        return paymentRepository.existsByOrderId(orderId);
    }
}
