package com.loopers.domain.order;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventPublisher orderEventPublisher;

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
    public Order findByIdWithLock(Long orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    @Transactional
    public Order order(User user,
                       List<OrderItem> orderItems,
                       List<OrderCommand.OrderItemCommand> orderItemCommands,
                       Address address,
                       BigDecimal totalAmount,
                       BigDecimal discountAmount,
                       BigDecimal usedPoint,
                       Long userCouponId) {
        Order order = Order.create(user, address, totalAmount, discountAmount, usedPoint);
        orderItems.forEach(order::addOrderItem);
        Order savedOrder = orderRepository.save(order);
        orderEventPublisher.publish(
                new OrderCreatedEvent(savedOrder.getId(),
                        orderItemCommands,
                        usedPoint,
                        userCouponId,
                        order.getPaidAmount(),
                        user.getId(),
                        discountAmount)
        );
        return savedOrder;
    }

    public void complete(Long orderId) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND));
        order.complete();
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_FOUND));
    }
}
