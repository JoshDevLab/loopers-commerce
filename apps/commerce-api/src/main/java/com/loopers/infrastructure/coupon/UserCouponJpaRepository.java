package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon, Long> {
    Optional<UserCoupon> findByIdAndUserId(Long id, Long userPk);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select uc from UserCoupon uc join fetch uc.coupon where uc.id = :userCouponId")
    Optional<UserCoupon> findByIdWithLock(Long userCouponId);
}
