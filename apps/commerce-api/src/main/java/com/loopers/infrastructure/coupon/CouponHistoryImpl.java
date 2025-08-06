package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponHistory;
import com.loopers.domain.coupon.CouponHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponHistoryImpl implements CouponHistoryRepository {
    private final CouponHistoryJpaRepository couponJpaRepository;

    @Override
    public CouponHistory save(CouponHistory history) {
        return couponJpaRepository.save(history);
    }
}
