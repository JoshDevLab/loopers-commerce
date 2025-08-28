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

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public BigDecimal calculateDiscountAmount(BigDecimal totalAmount) {
        BigDecimal discountAmount = this.coupon.calculateDiscountAmount(totalAmount);

        if (discountAmount.compareTo(totalAmount) >= 0) {
            throw new CoreException(ErrorType.INVALID_COUPON, "할인 금액이 결제 금액보다 크거나 같습니다.");
        }

        return discountAmount;
    }

    public void useCancel() {
        this.isUsed = false;
        this.usedAt = null;
    }
}
