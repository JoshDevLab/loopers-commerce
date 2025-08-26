package com.loopers.domain.product.like;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductLikeStatService {
    private final ProductLikeStatRepository productLikeStatRepository;

    @Transactional
    public void like(Long productId) {
        Optional<ProductLikeStat> productLikeStat = productLikeStatRepository.findByProductIdWithLock(productId);
        productLikeStat.ifPresent(ProductLikeStat::like);
        if (productLikeStat.isEmpty()) {
            ProductLikeStat createdProductLikeStat = ProductLikeStat.createLike(productId);
            try {
                productLikeStatRepository.save(createdProductLikeStat);
            } catch (DataIntegrityViolationException e) {
                productLikeStatRepository.findByProductIdWithLock(productId).ifPresent(ProductLikeStat::like);
            }
        }
    }

    @Transactional
    public void unLike(Long productId) {
        Optional<ProductLikeStat> productLikeStat = productLikeStatRepository.findByProductIdWithLock(productId);
        productLikeStat.ifPresent(ProductLikeStat::unLike);
        if (productLikeStat.isEmpty()) {
            ProductLikeStat createdProductLikeStat = ProductLikeStat.create(productId);
            productLikeStatRepository.save(createdProductLikeStat);
        }
    }
}
