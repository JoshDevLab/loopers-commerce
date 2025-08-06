package com.loopers.domain.coupon;

import com.loopers.domain.order.Order;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponHistoryRepository couponHistoryRepository;

    public UserCoupon getUserCoupon(Long userCouponId) {
        return userCouponRepository.findByIdWithLock(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND, "사용자의 쿠폰을 찾을 수 없습니다."));
    }

    public void use(UserCoupon userCoupon) {
        userCoupon.use();
    }

    public BigDecimal calculateDiscountAmount(UserCoupon userCoupon, BigDecimal totalAmount) {
        return userCoupon.calculateDiscountAmount(totalAmount);
    }

    public void createCouponUsingHistory(UserCoupon userCoupon, Order order, BigDecimal discountAmount) {
        CouponHistory history = CouponHistory.create(userCoupon, order, CouponHistory.CouponUsingType.ORDER_USE, discountAmount);
        couponHistoryRepository.save(history);
    }
}
