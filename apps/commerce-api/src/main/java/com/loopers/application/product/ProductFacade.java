package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.outbox.OutboxEventPublisher;
import com.loopers.domain.product.*;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.domain.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final BrandService brandService;
    private final ProductOptionService productOptionService;
    private final ProductLikeService productLikeService;
    private final OutboxEventPublisher outboxEventPublisher;

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

        ProductInfo result = ProductInfo.from(product, productOptions);
        if (productLikeService.existsByProductAndUser(productId, userInfo.id())) {
            result.liked();
        }
        outboxEventPublisher.publish(new ProductViewEvent(productId));
        return result;
    }

    @Transactional
    public boolean likeProduct(Long productId, Long userPk) {
        productLikeService.like(productId, userPk);
        return true;
    }

    @Transactional
    public boolean unLikeProduct(Long productId, Long userPk) {
        productLikeService.unLike(productId, userPk);
        return false;
    }

    public List<ProductInfo> getLikedProducts(Long userPk) {
        List<Long> productIds = productLikeService.getProductLikeByUser(userPk)
                .stream()
                .map(ProductLike::getProductId)
                .toList();

        if (productIds.isEmpty()) {
            return List.of();
        }

        List<Product> products = productService.findAllByIds(productIds);

        List<Long> brandIds = products.stream()
                .map(product -> product.getBrand().getId())
                .toList();

        List<Brand> brands = brandService.findAllByIds(brandIds);

        Map<Long, Brand> brandMap = brands.stream()
                .collect(Collectors.toMap(
                        Brand::getId,
                        brand -> brand
                ));

        List<ProductInfo> infos = products.stream()
                .map(product -> ProductInfo.from(product, brandMap.get(product.getBrand().getId())))
                .toList();

        infos.forEach(ProductInfo::liked);
        return infos;
    }
}
