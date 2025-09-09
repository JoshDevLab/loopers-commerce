package com.loopers.interfaces.api.product.dto;

import com.loopers.application.product.ProductInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private BigDecimal basicPrice;
    private String description;
    private String imageUrl;
    private String brandName;
    private int likeCount;
    private Boolean liked;
    private String categoryName;
    private String productStatus;
    private List<ProductOptionResponse> options;
    private Long ranking; // 랭킹 정보 추가 (순위에 없으면 null)

    public static ProductDetailResponse from(ProductInfo productInfo) {
        return new ProductDetailResponse(
                productInfo.getId(),
                productInfo.getName(),
                productInfo.getBasicPrice(),
                productInfo.getDescription(),
                productInfo.getImageUrl(),
                productInfo.getBrandName(),
                productInfo.getLikeCount(),
                productInfo.getLiked(),
                productInfo.getCategoryName(),
                productInfo.getProductStatus().name(),
                productInfo.getOptions() != null ?
                        productInfo.getOptions().stream()
                                .map(ProductOptionResponse::from)
                                .toList() : List.of(),
                null // ranking은 별도로 설정
        );
    }

    public static ProductDetailResponse from(ProductInfo productInfo, Long ranking) {
        return new ProductDetailResponse(
                productInfo.getId(),
                productInfo.getName(),
                productInfo.getBasicPrice(),
                productInfo.getDescription(),
                productInfo.getImageUrl(),
                productInfo.getBrandName(),
                productInfo.getLikeCount(),
                productInfo.getLiked(),
                productInfo.getCategoryName(),
                productInfo.getProductStatus().name(),
                productInfo.getOptions() != null ?
                        productInfo.getOptions().stream()
                                .map(ProductOptionResponse::from)
                                .toList() : List.of(),
                ranking
        );
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductOptionResponse{
        private Long productOptionId;
        private String size;
        private String color;
        private BigDecimal price;
        private String productOptionStatus;

        public static ProductOptionResponse from(ProductInfo.ProductOptionInfo productOptionInfo) {
            return new ProductOptionResponse(
                    productOptionInfo.getProductOptionId(),
                    productOptionInfo.getSize(),
                    productOptionInfo.getColor(),
                    productOptionInfo.getPrice(),
                    productOptionInfo.getProductOptionStatus()
            );
        }
    }
}
