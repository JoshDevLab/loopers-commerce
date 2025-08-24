package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponHistoryRepository couponHistoryRepository;

    public UserCoupon getUserCoupon(Long userCouponId) {
        return userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND, "사용자의 쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    public void use(Long userCouponId, Long orderId, BigDecimal discountAmount) {
        UserCoupon userCoupon = userCouponRepository.findByIdWithLock(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND, "사용자의 쿠폰을 찾을 수 없습니다."));
        userCoupon.use();
        CouponHistory couponHistory = CouponHistory.create(userCoupon, orderId, CouponHistory.CouponUsingType.ORDER_USE, discountAmount);
        couponHistoryRepository.save(couponHistory);
    }

    public BigDecimal calculateDiscountAmount(UserCoupon userCoupon, BigDecimal totalAmount) {
        return userCoupon.calculateDiscountAmount(totalAmount);
    }

    @Transactional
    public void recovery(Long orderId) {
        if (couponHistoryRepository.existsByOrderId(orderId)) {
            CouponHistory couponHistory = couponHistoryRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new CoreException(ErrorType.COUPON_HISTORY_NOT_FOUND, "쿠폰 이력을 찾을 수 없습니다."));
            UserCoupon userCoupon = userCouponRepository.findById(couponHistory.getUserCoupon().getId())
                    .orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND, "사용자의 쿠폰을 찾을 수 없습니다."));
            userCoupon.useCancel();
            couponHistoryRepository.delete(couponHistory);
        }
    }
}
