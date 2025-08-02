package com.loopers.domain.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;

import java.util.List;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);

    boolean existsByProductAndUser(Product product, User user);

    void deleteByProductAndUser(Product product, User user);

    List<ProductLike> findByUser(User user);
}
