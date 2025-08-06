package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponHistoryJpaRepository extends JpaRepository<CouponHistory, Integer> {
}
