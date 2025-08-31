package com.loopers.domain.product.like;

import com.loopers.domain.product.ProductChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductLikeService {
    private final ProductLikeRepository productLikeRepository;
    private final ProductChangedEventPublisher productChangedEventPublisher;
    private final ProductLikeEventPublisher productLikeEventPublisher;

    public boolean existsByProductAndUser(Long productId, Long userPk) {
        return productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
    }

    @Transactional
    public void like(Long productId, Long userPk) {
        boolean alreadyLiked = productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
        if (!alreadyLiked) {
            productLikeRepository.save(ProductLike.create(productId, userPk));
            productLikeEventPublisher.publish(new ProductLikeEvent(productId));
            productChangedEventPublisher.publish(new ProductChangedEvent(productId));
        }
    }

    @Transactional
    public void unLike(Long productId, Long userPk) {
        boolean alreadyLiked = productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
        if (alreadyLiked) {
            productLikeRepository.deleteByProductIdAndUserPk(productId, userPk);
            productLikeEventPublisher.publish(new ProductUnLikeEvent(productId));
            productChangedEventPublisher.publish(new ProductChangedEvent(productId));
        }
    }

    public List<ProductLike> getProductLikeByUser(Long userPk) {
        return productLikeRepository.findByUserPk(userPk);
    }
}
