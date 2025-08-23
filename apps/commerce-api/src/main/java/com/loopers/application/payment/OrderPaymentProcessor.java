package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PgOrderIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OrderPaymentProcessor {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public Payment completeOrderAndPayment(PaymentCommand.CallbackRequest command) {
        Order order = orderService.findByIdForUpdate(PgOrderIdGenerator.extractOrderId(command.orderId()));
        order.complete();

        Payment payment = paymentService.findByTransactionId(command.transactionKey());

        return paymentService.updateSuccessStatus(payment.getId());
    }

    @Transactional
    public Payment failedOrderAndPayment(PaymentCommand.CallbackRequest command) {
        Payment payment = paymentService.findByTransactionId(command.transactionKey());
        return paymentService.updateFailedStatus(payment.getId());
    }
}
