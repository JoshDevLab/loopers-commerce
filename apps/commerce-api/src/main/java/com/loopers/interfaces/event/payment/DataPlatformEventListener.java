package com.loopers.interfaces.event.payment;

import com.loopers.domain.order.OrderCreatedEvent;
import com.loopers.domain.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataPlatformEventListener {
    private final DataPlatformService dataPlatformService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentEvent.PaymentSuccess event) {
        dataPlatformService.send(event.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailure(PaymentEvent.PaymentFailedRecovery event) {
        dataPlatformService.send(event.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        dataPlatformService.send(event.orderId());
    }
}
