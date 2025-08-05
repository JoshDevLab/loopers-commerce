package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private boolean isUsed;

    public static UserCoupon create(User user, Coupon coupon, ZonedDateTime expiredAt) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.user = user;
        userCoupon.coupon = coupon;
        userCoupon.issuedAt = LocalDateTime.now();
        userCoupon.expiredAt = expiredAt.toLocalDateTime();
        userCoupon.isUsed = false;
        return userCoupon;
    }

    public void use() {
        isUsable();
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    public void isUsable() {
        if (this.isUsed) {
            throw new CoreException(ErrorType.ALREADY_USING_COUPON, "이미 사용된 쿠폰입니다.");
        }
        if (isExpired()) {
            throw new CoreException(ErrorType.EXPIRED_COUPON, "만료된 쿠폰입니다.");
        }
    }

    public BigDecimal calculateDiscountAmount(BigDecimal totalAmount) {
        if (coupon.getType() == Coupon.CouponType.RATE) {
            return totalAmount.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
        }
        return totalAmount.subtract(coupon.getDiscountValue());
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}
