package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<ProductOption> findById(Long productOptionId) {
        return productOptionJpaRepository.findById(productOptionId);
    }

    @Override
    public Optional<ProductOption> findByIdWithLock(Long productOptionId) {
        return productOptionJpaRepository.findByIdWithLock(productOptionId);
    }
}
