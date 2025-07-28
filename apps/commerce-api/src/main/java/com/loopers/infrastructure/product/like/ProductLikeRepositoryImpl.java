package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductLikeRepositoryImpl implements ProductLikeRepository {
    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public boolean existProductLikeByUser(Product product, User user) {
        return productLikeJpaRepository.existsByProductAndUser(product, user);
    }

    @Override
    public void save(ProductLike productLike) {
        productLikeJpaRepository.save(productLike);
    }
}
