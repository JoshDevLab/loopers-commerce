package com.loopers.domain.product.like;

import java.util.Optional;

public interface ProductLikeStatRepository {
    void save(ProductLikeStat productLikeStat);
    Optional<ProductLikeStat> findByProductIdWithLock(Long productId);
}
