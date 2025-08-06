package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderCriteria;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final OrderService orderService;
    private final PointService pointService;
    private final OrderItemProcessor orderItemProcessor;
    private final CouponProcessor couponProcessor;
    private final OrderCreator orderCreator;

    @Transactional
    public OrderInfo order(OrderCommand.Register command, Long userPk) {
        OrderItemProcessor.Result processedItems = orderItemProcessor.process(command.getOrderItemCommands());

        CouponProcessor.Result couponResult = couponProcessor.process(command.getUserCouponId(), processedItems.totalAmount());

        BigDecimal paidAmount = processedItems.totalAmount().subtract(couponResult.discountAmount());
        pointService.use(userPk, paidAmount);

        Order order = orderCreator.createOrder(userPk, processedItems.orderItems(), command.getAddress(),
                processedItems.totalAmount(), couponResult.discountAmount());

        orderCreator.saveInventoryHistories(processedItems.inventoryHistories(), order);

        if (couponResult.userCoupon() != null) {
            couponProcessor.createUsageHistory(couponResult.userCoupon(), order, couponResult.discountAmount());
        }

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
