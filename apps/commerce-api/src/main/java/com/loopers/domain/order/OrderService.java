package com.loopers.domain.order;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(User user, List<OrderItem> orderItems, Address address, BigDecimal totalAmount, BigDecimal discountAmount) {
        Order order = Order.create(user, address, totalAmount, discountAmount);
        orderItems.forEach(order::addOrderItem);
        return orderRepository.save(order);
    }

    public Page<Order> getOrdersWithCondition(OrderCriteria criteria, Long userPk, Pageable pageable) {
        return orderRepository.findAllByCriteriaAndUserPk(criteria, userPk, pageable);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다."));
        order.cancel();
    }

    @Transactional
    public Order findByIdForUpdate(Long orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다."));
    }
}
