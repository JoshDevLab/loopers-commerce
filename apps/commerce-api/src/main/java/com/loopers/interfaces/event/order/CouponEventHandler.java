package com.loopers.interfaces.event.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventHandler {
    private final CouponService couponService;

    @Order(3)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling order created event CouponEventHandler: {}", event);

        if (event.userCouponId() != null) {
            couponService.use(event.userCouponId(), event.orderId(), event.discountAmount());
        }
    }
}
