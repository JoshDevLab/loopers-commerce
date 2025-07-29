package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    Page<Product> findAllByCriteria(ProductCriteria criteria, Pageable pageable);

    Optional<Product> findById(Long productId);

    Optional<Product> findWithBrandById(Long productId);

    List<Product> findByBrandId(Brand brand);
}
