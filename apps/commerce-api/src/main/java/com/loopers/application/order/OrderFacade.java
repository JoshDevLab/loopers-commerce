package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryHistory;
import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.*;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final OrderService orderService;
    private final ProductOptionService productOptionService;
    private final InventoryService inventoryService;
    private final PointService pointService;
    private final UserService userService;
    private final CouponService couponService;

    @Transactional
    public OrderInfo order(OrderCommand.Register orderRegisterCommand, Long userPk) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        List<InventoryHistory> inventoryHistories = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderCommand.OrderItemCommand orderItemCommand : orderRegisterCommand.getOrderItemCommands()) {
            ProductOption productOption = productOptionService.getOnSalesProductOption(orderItemCommand.getProductOptionId());
            Inventory inventory = inventoryService.getEnoughQuantityInventory(productOption, orderItemCommand.getQuantity());
            inventoryHistories.add(InventoryHistory.createOrderHistory(inventory, orderItemCommand.getQuantity()));
            inventoryService.decreaseQuantity(inventory, orderItemCommand.getQuantity());

            orderItems.add(OrderItem.create(productOption, orderItemCommand.getQuantity()));

            totalAmount = totalAmount.add(productOption.getPrice().multiply(BigDecimal.valueOf(orderItemCommand.getQuantity())));
        }

        // 쿠폰 사용
        // TODO

        pointService.use(userPk, totalAmount);

        User user = userService.getMyInfoByUserPk(userPk);
        Order order = orderService.createOrder(orderRegisterCommand, totalAmount, orderItems, user);

        inventoryHistories.forEach(inventoryHistory -> inventoryService.createInventoryHistory(inventoryHistory.setOrder(order)));
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
