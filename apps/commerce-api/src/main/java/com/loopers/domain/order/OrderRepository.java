package com.loopers.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Page<Order> findAllByCriteriaAndUserPk(OrderCriteria criteria, Long userPk, Pageable pageable);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByIdWithLock(Long orderId);
}
