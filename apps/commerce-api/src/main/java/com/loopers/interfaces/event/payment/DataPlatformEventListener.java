package com.loopers.interfaces.event.payment;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCreatedEvent;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataPlatformEventListener {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final DataPlatformService dataPlatformService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentEvent.PaymentSuccess event) {
        Order order = orderService.findById(event.orderId());
        List<Payment> payments = paymentService.findByOrderId(event.orderId());
        DataPlatformService.DataPlatformRequest request =
                new DataPlatformService.DataPlatformRequest(OrderInfo.from(order), payments.stream().map(PaymentInfo::of).toList());
        dataPlatformService.send(request);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailure(PaymentEvent.PaymentFailedRecovery event) {
        Order order = orderService.findById(event.orderId());
        List<Payment> payments = paymentService.findByOrderId(event.orderId());
        DataPlatformService.DataPlatformRequest request =
                new DataPlatformService.DataPlatformRequest(OrderInfo.from(order), payments.stream().map(PaymentInfo::of).toList());
        dataPlatformService.send(request);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = orderService.findById(event.orderId());
        List<Payment> payments = paymentService.findByOrderId(event.orderId());
        DataPlatformService.DataPlatformRequest request =
                new DataPlatformService.DataPlatformRequest(OrderInfo.from(order), payments.stream().map(PaymentInfo::of).toList());
        dataPlatformService.send(request);
    }
}
