package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProductLikeRepositoryImpl implements ProductLikeRepository {
    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public ProductLike save(ProductLike productLike) {
        return productLikeJpaRepository.save(productLike);
    }

    @Override
    public boolean existsByProductIdAndUserPk(Long productId, Long userPk) {
        return productLikeJpaRepository.existsByProductIdAndUserPk(productId, userPk);
    }

    @Override
    public void deleteByProductIdAndUserPk(Long productId, Long userPk) {
        productLikeJpaRepository.deleteByProductAndUser(productId, userPk);
    }

    @Override
    public List<ProductLike> findByUserPk(Long userPk) {
        return productLikeJpaRepository.findByUserPk(userPk);
    }
}
