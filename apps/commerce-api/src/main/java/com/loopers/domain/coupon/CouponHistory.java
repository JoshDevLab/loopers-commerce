package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_history")
public class CouponHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id", nullable = false)
    private UserCoupon userCoupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponUsingType usingType;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    public static CouponHistory create(UserCoupon userCoupon, Order order, CouponUsingType usingType, BigDecimal discountAmount) {
        CouponHistory history = new CouponHistory();
        history.userCoupon = userCoupon;
        history.order = order;
        history.usingType = usingType;
        history.discountAmount = discountAmount;
        return history;
    }

    public enum CouponUsingType {
        ORDER_USE,   // 주문에 사용
        MANUAL_USE   // 관리자 수동 차감
    }
}
