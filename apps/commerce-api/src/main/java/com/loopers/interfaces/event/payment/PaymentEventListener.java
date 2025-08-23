package com.loopers.interfaces.event.payment;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentEvent;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventListener {
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PointService pointService;
    private final CouponService couponService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentEvent.PaymentFailedRecovery event) {
        log.info("결제 실패 복구 이벤트 처리 시작: orderId={}", event.orderId());
        
        try {
            orderService.cancel(event.orderId());
            inventoryService.recovery(event.orderId());
            pointService.recovery(event.orderId());
            couponService.recovery(event.orderId());
            
            log.info("결제 실패 복구 이벤트 처리 완료: orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("결제 실패 복구 이벤트 처리 중 오류 발생: orderId={}", event.orderId(), e);
        }
    }
}
