package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponHistory;
import com.loopers.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponHistoryJpaRepository extends JpaRepository<CouponHistory, Integer> {
    Optional<CouponHistory> findByOrderId(Long orderId);

    List<CouponHistory> order(Order order);

    boolean existsByOrderId(Long orderId);
}
