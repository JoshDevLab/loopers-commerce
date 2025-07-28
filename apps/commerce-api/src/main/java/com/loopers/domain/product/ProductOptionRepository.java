package com.loopers.domain.product;

import java.util.List;

public interface ProductOptionRepository {
    ProductOption save(ProductOption productOption);

    List<ProductOption> findByProductId(Long productId);
}
