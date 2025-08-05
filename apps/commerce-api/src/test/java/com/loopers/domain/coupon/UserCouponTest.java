package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import com.loopers.domain.user.User;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class UserCouponTest {

    private User user;
    private Coupon rateCoupon; // 정률 쿠폰 20%
    private Coupon amountCoupon; // 정액 쿠폰 5000원

    @BeforeEach
    void setUp() {
        user = User.create("userId", "user@email.com", "1990-01-01", "MALE");
        rateCoupon = Coupon.create("정률 쿠폰", Coupon.CouponType.RATE, BigDecimal.valueOf(20));
        amountCoupon = Coupon.create("정액 쿠폰", Coupon.CouponType.FIXED_AMOUNT, BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("쿠폰이 사용되지 않았고, 유효기간 내이면 사용 가능하다")
    void isUsable_validCoupon_doesNotThrow() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().plusDays(1));

        // Act & Assert
        assertThatCode(userCoupon::isUsable)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 사용한 쿠폰이면 ErrorType.ALREADY_USING_COUPON 예외가 발생한다")
    void isUsable_usedCoupon_throwsException() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().plusDays(1));
        userCoupon.use();

        // Act & Assert
        assertThatThrownBy(userCoupon::isUsable)
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.ALREADY_USING_COUPON);
    }

    @Test
    @DisplayName("만료된 쿠폰이면 ErrorType.EXPIRED_COUPON 예외가 발생한다")
    void isUsable_expiredCoupon_throwsException() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().minusDays(1));

        // Act & Assert
        assertThatThrownBy(userCoupon::isUsable)
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.EXPIRED_COUPON);
    }

    @Test
    @DisplayName("쿠폰을 사용하면 used 상태가 true로 변경되고 usedAt이 세팅된다")
    void use_setsUsedAndTimestamp() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().plusDays(1));

        // Act
        userCoupon.use();

        // Assert
        assertThat(userCoupon.isUsed()).isTrue();
        assertThat(userCoupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("정률 쿠폰은 총 금액에서 비율만큼 할인 금액을 계산한다")
    void calculateDiscountAmount_rateCoupon() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().plusDays(1));
        BigDecimal totalAmount = BigDecimal.valueOf(10000);

        // Act
        BigDecimal discount = userCoupon.calculateDiscountAmount(totalAmount);

        // Assert
        assertThat(discount).isEqualByComparingTo("2000"); // 20%
    }

    @Test
    @DisplayName("정액 쿠폰은 총 금액에서 고정 금액을 할인한다")
    void calculateDiscountAmount_amountCoupon() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, amountCoupon, ZonedDateTime.now().plusDays(1));
        BigDecimal totalAmount = BigDecimal.valueOf(10000);

        // Act
        BigDecimal discount = userCoupon.calculateDiscountAmount(totalAmount);

        // Assert
        assertThat(discount).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("isExpired는 유효기간이 지났으면 true를 반환한다")
    void isExpired_trueIfPastExpireAt() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().minusSeconds(1));

        // Act
        boolean expired = userCoupon.isExpired();

        // Assert
        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("isExpired는 아직 유효기간 전이면 false를 반환한다")
    void isExpired_falseIfNotExpiredYet() {
        // Arrange
        UserCoupon userCoupon = UserCoupon.create(user, rateCoupon, ZonedDateTime.now().plusDays(1));

        // Act
        boolean expired = userCoupon.isExpired();

        // Assert
        assertThat(expired).isFalse();
    }
}
