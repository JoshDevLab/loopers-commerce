package com.loopers.application.product;

import com.loopers.domain.product.*;
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

    @Transactional(readOnly = true)
    public Page<ProductInfo> getProductsWithCondition(ProductCriteria criteria, Pageable pageable) {
        return productService.searchByConditionWithPaging(criteria, pageable)
                .map(ProductInfo::from);
    }

    @Transactional(readOnly = true)
    public ProductInfo getProductDetail(Long productId, UserInfo userInfo) {
        Product product = productService.getProductWithBrandById(productId);
        List<ProductOption> productOptions = productOptionService.getProductOptionsByProductId(productId);

        if (userInfo == null) {
            return ProductInfo.from(product, productOptions);
        }

        User user = userService.getMyInfoByUserPk(userInfo.id());

        ProductInfo result = ProductInfo.from(product, productOptions);
        if (productLikeService.isProductLikedByUser(product, user)) {
            result.liked();
        }
        return result;
    }
}
