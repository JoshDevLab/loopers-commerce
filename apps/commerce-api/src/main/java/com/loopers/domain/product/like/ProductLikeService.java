package com.loopers.domain.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductLikeService {
    private final ProductLikeRepository productLikeRepository;

    public boolean existsByProductAndUser(Product product, User user) {
        return productLikeRepository.existsByProductAndUser(product, user);
    }

    @Transactional
    public void like(Product product, User user) {
        boolean alreadyLiked = productLikeRepository.existsByProductAndUser(product, user);
        if (!alreadyLiked) {
            productLikeRepository.save(ProductLike.create(product, user));
            product.increaseLikeCount();
        }
    }

    @Transactional
    public void unLike(Product product, User user) {
        boolean alreadyLiked = productLikeRepository.existsByProductAndUser(product, user);
        if (alreadyLiked) {
            productLikeRepository.deleteByProductAndUser(product, user);
            product.decreaseLikeCount();
        }
    }

    public List<ProductLike> getProductLikeByUser(User user) {
        return productLikeRepository.findByUser(user);
    }
}
