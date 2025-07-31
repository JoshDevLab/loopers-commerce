package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
    @Query("select oi from OrderItem oi join fetch oi.productOption where oi.order = :order")
    List<OrderItem> findAllByOrderWithProductOption(Order order);
}
