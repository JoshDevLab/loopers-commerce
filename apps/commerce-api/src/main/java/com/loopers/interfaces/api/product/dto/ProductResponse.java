package com.loopers.interfaces.api.product.dto;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse{
    Long productId;
    String name;
    BigDecimal basicPrice;
    String brandName;
    int likeCount;
    Boolean liked;
    String imageUrl;
    String categoryName;
    ProductStatus productStatus;
    String description;

    public static ProductResponse from(ProductInfo productInfo) {
        return new ProductResponse(
            productInfo.getId(),
            productInfo.getName(),
            productInfo.getBasicPrice(),
            productInfo.getBrandName(),
            productInfo.getLikeCount(),
            productInfo.getLiked(),
            productInfo.getImageUrl(),
            productInfo.getCategoryName(),
            productInfo.getProductStatus(),
            productInfo.getDescription()
        );
    }
}
