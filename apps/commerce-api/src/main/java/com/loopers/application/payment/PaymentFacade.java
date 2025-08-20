package com.loopers.application.payment;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final PointService pointService;

    public PaymentInfo payment(PaymentCommand.Request paymentCommand) {
        Order order = orderService.findByIdForUpdate(paymentCommand.orderId());

        if (paymentService.existsByOrderIdAndStatus(order.getId(), Payment.PaymentStatus.SUCCESS)) {
            throw new CoreException(ErrorType.ALREADY_EXIST_ORDER_PAYMENT, order.getId() + " 는 이미 결제가 완료된 주문입니다.");
        }

        Payment payment = paymentService.create(paymentCommand);

        try {
            paymentService.payment(paymentCommand);
        } catch (CoreException e) {
            log.error("외부 PG 결제 실패", e);
            recoveryAll(paymentCommand.orderId());
            throw new CoreException(ErrorType.PAYMENT_FAIL, "외부 결제 실패로 복구 처리함");
        }

        order.complete();
        Payment updated = paymentService.updateSuccessStatus(payment.getId());
        return PaymentInfo.of(updated);
    }

    // Todo: 이벤트 발행 코드로 변경
    private void recoveryAll(Long orderId) {
        orderService.cancel(orderId);
        inventoryService.recovery(orderId);
        pointService.recovery(orderId);
        couponService.recovery(orderId);
    }

}
