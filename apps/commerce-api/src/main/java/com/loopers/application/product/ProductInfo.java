package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStatus;

import java.math.BigDecimal;

public record ProductInfo(
    Long id,
    String name,
    String description,
    String categoryName,
    Long brandId,
    String brandName,
    String imageUrl,
    BigDecimal basicPrice,
    ProductStatus productStatus,
    int likeCount
) {
    public static ProductInfo from(Product product) {
        return new ProductInfo(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getProductCategory().name(),
            product.getBrand().getId(),
            product.getBrand().getName(),
            product.getImageUrl(),
            product.getBasicPrice(),
            product.getProductStatus(),
            product.getLikeCount()
        );
    }
}
