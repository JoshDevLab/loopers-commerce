package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findByProductId(Long productId);

    Optional<ProductOption> findByIdAndProductOptionStatus(Long productOptionId, ProductStatus productStatus);
}
