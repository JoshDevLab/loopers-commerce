package com.loopers.application.order;

import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryHistory;
import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.point.PointService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public OrderInfo order(OrderCommand.Register orderRegisterCommand, Long userPk) {
        BigDecimal paidAmount = BigDecimal.ZERO;
        List<InventoryHistory> inventoryHistories = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderCommand.OrderItemCommand orderItemCommand : orderRegisterCommand.getOrderItemCommands()) {
            ProductOption productOption = productOptionService.getProductOption(orderItemCommand.getProductOptionId());
            productOption.isOnSales();

            Inventory inventory = inventoryService.hasEnoughQuantity(productOption, orderItemCommand.getQuantity());
            inventoryHistories.add(InventoryHistory.createOrderHistory(inventory, orderItemCommand.getQuantity()));
            inventoryService.decreaseQuantity(inventory, orderItemCommand.getQuantity());

            orderItems.add(OrderItem.create(productOption, orderItemCommand.getQuantity()));

            paidAmount = paidAmount.add(productOption.getPrice().multiply(BigDecimal.valueOf(orderItemCommand.getQuantity())));
        }
        pointService.use(userPk, paidAmount);

        User user = userService.getMyInfoByUserPk(userPk);
        Order order = orderService.createOrder(orderRegisterCommand, paidAmount, orderItems, user);

        inventoryHistories.forEach(inventoryHistory -> inventoryHistory.setOrder(order));
        return OrderInfo.from(order);
    }
}
