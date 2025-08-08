package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponHistoryRepository {
    CouponHistory save(CouponHistory history);

    Optional<CouponHistory> findByOrderId(Long orderId);

    void delete(CouponHistory couponHistory);

    boolean existsByOrderId(Long orderId);
}
