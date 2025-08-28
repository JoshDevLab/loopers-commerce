package com.loopers.domain.product.like;

import java.util.List;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);

    boolean existsByProductIdAndUserPk(Long productId, Long userPk);

    void deleteByProductIdAndUserPk(Long productId, Long userPk);

    List<ProductLike> findByUserPk(Long userPk);
}
