package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    @Query("select o from Order o join fetch o.user join fetch o.orderItems oi join fetch oi.productOption where o.id = :orderId")
    Optional<Order> findByIdWithFetch(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdWithLock(Long orderId);
}
