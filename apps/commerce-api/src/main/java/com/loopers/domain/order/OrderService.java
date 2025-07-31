package com.loopers.domain.order;

import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(OrderCommand.Register orderRegisterCommand, BigDecimal paidAmount, List<OrderItem> orderItems, User user) {
        Order order = Order.create(user, orderRegisterCommand, paidAmount);
        orderItems.forEach(order::addOrderItem);
        return orderRepository.save(order);
    }

    public Page<Order> getOrdersWithCondition(OrderCriteria criteria, Long userPk, Pageable pageable) {
        return orderRepository.findAllByCriteriaAndUserPk(criteria, userPk, pageable);
    }
}
