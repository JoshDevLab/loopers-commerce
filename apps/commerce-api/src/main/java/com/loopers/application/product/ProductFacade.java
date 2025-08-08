package com.loopers.application.product;

import com.loopers.domain.product.*;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final ProductOptionService productOptionService;
    private final ProductLikeService productLikeService;
    private final UserService userService;

    public Page<ProductInfo> getProductsWithCondition(ProductCriteria criteria, Pageable pageable) {
        return productService.searchByConditionWithPaging(criteria, pageable)
                .map(ProductInfo::from);
    }

    public ProductInfo getProductDetail(Long productId, UserInfo userInfo) {
        Product product = productService.getProductWithBrandById(productId);
        List<ProductOption> productOptions = productOptionService.getProductOptionsByProductId(productId);

        if (userInfo == null) {
            return ProductInfo.from(product, productOptions);
        }

        User user = userService.getMyInfoByUserPk(userInfo.id());

        ProductInfo result = ProductInfo.from(product, productOptions);
        if (productLikeService.existsByProductAndUser(product, user)) {
            result.liked();
        }
        return result;
    }

    @Transactional
    public ProductLikedInfo likeProduct(Long productId, Long userPk) {
        Product product = productService.getProductByIdWithLock(productId);
        User user = userService.getMyInfoByUserPk(userPk);
        productLikeService.like(product, user);
        return new ProductLikedInfo(true, product.getLikeCount());
    }

    @Transactional
    public ProductLikedInfo unLikeProduct(Long productId, Long userPk) {
        Product product = productService.getProductByIdWithLock(productId);
        User user = userService.getMyInfoByUserPk(userPk);
        productLikeService.unLike(product, user);
        return new ProductLikedInfo(false, product.getLikeCount());
    }

    public List<ProductInfo> getLikedProducts(Long userPk) {
        User user = userService.getMyInfoByUserPk(userPk);
        List<ProductLike> productLikes = productLikeService.getProductLikeByUser(user);
        List<Product> products = productLikes
                .stream()
                .map(ProductLike::getProduct)
                .toList();
        List<ProductInfo> productInfos = products.stream()
                .map(ProductInfo::from)
                .toList();
        productInfos.forEach(ProductInfo::liked);
        return productInfos;
    }
}
