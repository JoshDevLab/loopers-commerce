package com.loopers.interfaces.event.order;

import com.loopers.domain.order.OrderCreatedEvent;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Component
public class PointEventHandler {
    private final PointService pointService;

    @Order(2)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling order created event PointEventHandler: {}", event);
        if (event.usedPoint().compareTo(BigDecimal.ZERO) > 0) {
            pointService.use(event.userPk(), event.usedPoint(), event.orderId());
        }
    }
}
