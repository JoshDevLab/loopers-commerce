package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.user.User;
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
    public boolean existsByProductAndUser(Product product, User user) {
        return productLikeJpaRepository.existsByProductAndUser(product, user);
    }

    @Override
    public void deleteByProductAndUser(Product product, User user) {
        productLikeJpaRepository.deleteByProductAndUser(product, user);
    }

    @Override
    public List<ProductLike> findByUser(User user) {
        return productLikeJpaRepository.findByUser(user);
    }
}
