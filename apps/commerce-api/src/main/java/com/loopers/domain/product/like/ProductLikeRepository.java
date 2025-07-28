package com.loopers.domain.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;

public interface ProductLikeRepository {
    boolean existProductLikeByUser(Product product, User user);

    void save(ProductLike productLike);
}
