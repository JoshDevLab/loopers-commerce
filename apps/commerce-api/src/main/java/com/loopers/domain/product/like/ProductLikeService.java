package com.loopers.domain.product.like;

import com.loopers.domain.outbox.OutboxEventPublisher;
import com.loopers.domain.product.ProductChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductLikeService {
    private final ProductLikeRepository productLikeRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    public boolean existsByProductAndUser(Long productId, Long userPk) {
        return productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
    }

    @Transactional
    public void like(Long productId, Long userPk) {
        boolean alreadyLiked = productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
        if (!alreadyLiked) {
            productLikeRepository.save(ProductLike.create(productId, userPk));
            outboxEventPublisher.publish(new ProductLikeEvent(productId));
        }
    }

    @Transactional
    public void unLike(Long productId, Long userPk) {
        boolean alreadyLiked = productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
        if (alreadyLiked) {
            productLikeRepository.deleteByProductIdAndUserPk(productId, userPk);
            outboxEventPublisher.publish(new ProductLikeEvent(productId));
        }
    }

    public List<ProductLike> getProductLikeByUser(Long userPk) {
        return productLikeRepository.findByUserPk(userPk);
    }
}
