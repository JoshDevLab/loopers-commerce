package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponHistoryJpaRepository extends JpaRepository<CouponHistory, Integer> {
    Optional<CouponHistory> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
