package com.loopers.interfaces.event.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventHandler {
    private final CouponService couponService;

    @Order(3)
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling order created event CouponEventHandler: {}", event);

        if (event.userCouponId() != null) {
            couponService.use(event.userCouponId(), event.orderId(), event.discountAmount());
        }
    }
}
