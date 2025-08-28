package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.like.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {
    boolean existsByProductIdAndUserPk(Long productId, Long userPk);

    @Modifying
    @Query("DELETE FROM ProductLike pl WHERE pl.productId = :productId AND pl.userPk = :userPk")
    void deleteByProductAndUser(Long productId, Long userPk);

    List<ProductLike> findByUserPk(Long userPk);
}
