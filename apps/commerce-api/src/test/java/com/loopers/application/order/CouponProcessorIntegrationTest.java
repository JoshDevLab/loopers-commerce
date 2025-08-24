package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CouponProcessorIntegrationTest extends IntegrationTestSupport {
    @Autowired
    CouponProcessor couponProcessor;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    UserRepository userRepository;

    @DisplayName("쿠폰을 사용하고 할인금액을 계산할 수 있다.")
    @Test
    void couponUsingAndReturnDiscountAmount() {
        // Arrange
        User user = userRepository.save(User.create("userId", "email@email.com", "1990-01-01", "MALE"));
        Coupon coupon = couponRepository.save(Coupon.create("20% 할인", Coupon.CouponType.RATE, BigDecimal.valueOf(20)));
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(user, coupon, ZonedDateTime.now().plusDays(1)));

        BigDecimal totalAmount = BigDecimal.valueOf(50000);

        // Act
        CouponProcessor.Result result = couponProcessor.process(userCoupon.getId(), totalAmount);

        // Assert
        assertThat(result.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(result.userCoupon().isUsed()).isFalse();
    }
}
