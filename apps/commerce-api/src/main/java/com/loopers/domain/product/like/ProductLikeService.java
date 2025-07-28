package com.loopers.domain.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductLikeService {
    private final ProductLikeRepository productLikeRepository;

    public boolean isProductLikedByUser(Product product, User user) {
        return productLikeRepository.existProductLikeByUser(product, user);
    }
}
