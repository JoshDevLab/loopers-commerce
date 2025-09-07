package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderCriteria;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductOptionService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final OrderService orderService;
    private final UserService userService;
    private final OrderItemProcessor orderItemProcessor;
    private final CouponProcessor couponProcessor;
    private final ProductOptionService productOptionService;

    public OrderInfo order(OrderCommand.Register command, Long userPk) {
        // order item 생성 및 토탈 금액 반환
        OrderItemProcessor.Result orderItemResult = orderItemProcessor.process(command.getOrderItemCommands());

        // coupon 사용 할인금액 반환
        CouponProcessor.Result couponResult = couponProcessor.process(command.getUserCouponId(), orderItemResult.totalAmount());

        // 유저 정보 조회
        User user = userService.getMyInfoByUserPk(userPk);

        List<Long> optionIds = command.getOrderItemCommands().stream().map(OrderCommand.OrderItemCommand::getProductOptionId).toList();
        List<Long> productIds = productOptionService.getProductsByOptionIdNotDuplicate(optionIds);

        Order order = orderService.order(user,
                orderItemResult.orderItems(),
                command.getOrderItemCommands(),
                command.getAddress(),
                orderItemResult.totalAmount(),
                couponResult.discountAmount(),
                command.getUsedPoint(),
                command.getUserCouponId(),
                productIds);

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
