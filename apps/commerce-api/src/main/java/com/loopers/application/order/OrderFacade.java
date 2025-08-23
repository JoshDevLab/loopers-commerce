package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderCriteria;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.loopers.domain.order.QOrder.order;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final OrderService orderService;
    private final PointService pointService;
    private final OrderItemProcessor orderItemProcessor;
    private final CouponProcessor couponProcessor;
    private final OrderCreator orderCreator;
    private final UserService userService;

    @Transactional
    public OrderInfo order(OrderCommand.Register command, Long userPk) {
//        Point point = null;
//        OrderItemProcessor.Result processedItems = orderItemProcessor.process(command.getOrderItemCommands());
//
//        CouponProcessor.Result couponResult = couponProcessor.process(command.getUserCouponId(), processedItems.totalAmount());
//
//        BigDecimal paidAmount = processedItems.totalAmount().subtract(couponResult.discountAmount());
//
//        if (command.getUsedPoint().compareTo(BigDecimal.ZERO) > 0) {
//            point = pointService.use(userPk, paidAmount);
//        }
//
//        Order order = orderCreator.createOrder(userPk, processedItems.orderItems(), command.getAddress(),
//                processedItems.totalAmount(), couponResult.discountAmount(), command.getUsedPoint());
//
//        orderCreator.saveInventoryHistories(processedItems.inventoryHistories(), order);
//
//        if (command.getUsedPoint().compareTo(BigDecimal.ZERO) > 0) {
//            pointService.createUsingPointHistory(point, paidAmount, order);
//        }
//
//        if (couponResult.userCoupon() != null) {
//            couponProcessor.createUsageHistory(couponResult.userCoupon(), order, couponResult.discountAmount());
//        }

        // order item 생성 및 토탈 금액 반환
        OrderItemProcessor.Result orderItemResult = orderItemProcessor.process(command.getOrderItemCommands());

        // coupon 사용 금액 반환
        CouponProcessor.Result couponResult = couponProcessor.process(command.getUserCouponId(), orderItemResult.totalAmount());

        User user = userService.getMyInfoByUserPk(userPk);

        Order order = orderService.order(user,
                orderItemResult.orderItems(),
                command.getOrderItemCommands(),
                command.getAddress(),
                orderItemResult.totalAmount(),
                couponResult.discountAmount(),
                command.getUsedPoint(),
                command.getUserCouponId());

        return OrderInfo.from(order);
    }


    public Page<OrderInfo> getOrdersWithCondition(OrderCriteria criteria, Long userPk, Pageable pageable) {
        return orderService.getOrdersWithCondition(criteria, userPk, pageable).map(OrderInfo::fromForSearch);
    }

    public OrderDetailInfo getOrderDetail(Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return OrderDetailInfo.from(order, order.getOrderItems());
    }


}
