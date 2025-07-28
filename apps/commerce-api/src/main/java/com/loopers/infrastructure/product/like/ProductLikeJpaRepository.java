package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {
    boolean existsByProductAndUser(Product product, User user);
}
