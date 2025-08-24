package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponHistory;
import com.loopers.domain.coupon.CouponHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CouponHistoryRepositoryImpl implements CouponHistoryRepository {
    private final CouponHistoryJpaRepository couponJpaRepository;

    @Override
    public CouponHistory save(CouponHistory history) {
        return couponJpaRepository.save(history);
    }

    @Override
    public Optional<CouponHistory> findByOrderId(Long orderId) {
        return couponJpaRepository.findByOrderId(orderId);
    }

    @Override
    public void delete(CouponHistory couponHistory) {
        couponJpaRepository.delete(couponHistory);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return couponJpaRepository.existsByOrderId(orderId);
    }
}
