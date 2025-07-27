package com.loopers.interfaces.api.product.dto;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.product.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
    Long productId,
    String name,
    BigDecimal basicPrice,
    String brandName,
    int likeCount,
    String imageUrl,
    String categoryName,
    ProductStatus productStatus
) {
    public static ProductResponse from(ProductInfo productInfo) {
        return new ProductResponse(
            productInfo.id(),
            productInfo.name(),
            productInfo.basicPrice(),
            productInfo.brandName(),
            productInfo.likeCount(),
            productInfo.imageUrl(),
            productInfo.categoryName(),
            productInfo.productStatus()
        );
    }
}
