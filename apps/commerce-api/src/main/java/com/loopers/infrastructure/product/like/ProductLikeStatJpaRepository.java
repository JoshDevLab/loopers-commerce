package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.like.ProductLikeStat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductLikeStatJpaRepository extends JpaRepository<ProductLikeStat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pls from ProductLikeStat pls where pls.productId = :productId")
    Optional<ProductLikeStat> findByProductIdWithLock(Long productId);
}
