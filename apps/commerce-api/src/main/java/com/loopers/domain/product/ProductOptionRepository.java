package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository {
    ProductOption save(ProductOption productOption);

    List<ProductOption> findByProductId(Long productId);

    Optional<ProductOption> findById(Long productOptionId);

    Optional<ProductOption> findByIdWithLock(Long productOptionId);
}
