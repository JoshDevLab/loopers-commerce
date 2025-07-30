package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {
    boolean existsByProductAndUser(Product product, User user);

    @Modifying
    @Query("DELETE FROM ProductLike pl WHERE pl.product = :product AND pl.user = :user")
    void deleteByProductAndUser(Product product, User user);

    @Query("""
        SELECT pl 
        FROM ProductLike pl
        JOIN FETCH pl.product p
        JOIN FETCH p.brand
        JOIN FETCH pl.user
        WHERE pl.user = :user
    """)
    List<ProductLike> findByUser(User user);
}
