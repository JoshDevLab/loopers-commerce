package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final ProductOptionJpaRepository productOptionJpaRepository;

    @Override
    public ProductOption save(ProductOption productOption) {
        return productOptionJpaRepository.save(productOption);
    }

    @Override
    public List<ProductOption> findByProductId(Long productId) {
        return productOptionJpaRepository.findByProductId(productId);
    }
}
