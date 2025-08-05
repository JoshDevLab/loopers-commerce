package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    public static Coupon create(String name, CouponType type, BigDecimal discountValue) {
        Coupon coupon = new Coupon();
        coupon.name = name;
        coupon.type = type;
        coupon.discountValue = discountValue;
        return coupon;
    }

    public enum CouponType {
        FIXED_AMOUNT, RATE
    }
}
