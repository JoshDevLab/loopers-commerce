package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.like.ProductLikeStat;
import com.loopers.domain.product.like.ProductLikeStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductLikeStatRepositoryImpl implements ProductLikeStatRepository {
    private final ProductLikeStatJpaRepository productLikeStatJpaRepository;

    @Override
    public void save(ProductLikeStat productLikeStat) {
        productLikeStatJpaRepository.save(productLikeStat);
    }

    @Override
    public Optional<ProductLikeStat> findByProductIdWithLock(Long productId) {
        return productLikeStatJpaRepository.findByProductIdWithLock(productId);
    }
}
