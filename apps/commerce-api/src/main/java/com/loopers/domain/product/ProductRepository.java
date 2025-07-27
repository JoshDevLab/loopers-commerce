package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Product save(Product product);

    Page<Product> findAllByCriteria(ProductCriteria criteria, Pageable pageable);
}
