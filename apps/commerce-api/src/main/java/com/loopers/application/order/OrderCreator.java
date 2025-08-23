package com.loopers.application.order;

import com.loopers.domain.inventory.InventoryHistory;
import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.Address;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderCreator {

    private final UserService userService;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    @Transactional
    public Order createOrder(
            Long userPk,
            List<OrderItem> items,
            Address address,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal usedPoint) {
        User user = userService.getMyInfoByUserPk(userPk);
        return orderService.createOrder(user, items, address, totalAmount, discountAmount, usedPoint);
    }

}

