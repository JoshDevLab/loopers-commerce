package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class CouponProcessor {

    private final CouponService couponService;

    @Transactional
    public Result process(Long userCouponId, BigDecimal totalAmount) {
        if (userCouponId == null) return new Result(null, BigDecimal.ZERO);

        UserCoupon coupon = couponService.getUserCoupon(userCouponId);
        couponService.use(coupon);
        BigDecimal discountAmount = couponService.calculateDiscountAmount(coupon, totalAmount);

        return new Result(coupon, discountAmount);
    }

    public void createUsageHistory(UserCoupon userCoupon, Order order, BigDecimal discountAmount) {
        couponService.createCouponUsingHistory(userCoupon, order, discountAmount);
    }

    public record Result(UserCoupon userCoupon, BigDecimal discountAmount) {}
}

