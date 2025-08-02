package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String imageUrl;
    private BigDecimal basicPrice;
    private ProductStatus productStatus;
    private int likeCount;
    private Boolean liked;
    private List<ProductOptionInfo> options;

    private ProductInfo(Long id, String name, String description, String categoryName, Long brandId, String brandName,
                       String imageUrl, BigDecimal basicPrice, ProductStatus productStatus, int likeCount,
                       List<ProductOptionInfo> options) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
        this.imageUrl = imageUrl;
        this.basicPrice = basicPrice;
        this.productStatus = productStatus;
        this.likeCount = likeCount;
        this.liked = false;
        this.options = options;
    }

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
            product.getLikeCount(),
            null
        );
    }

    public static ProductInfo from(Product product, List<ProductOption> productOptions) {
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
                product.getLikeCount(),
                productOptions.stream().map(ProductOptionInfo::from).toList()
        );
    }

    public void liked() {
        this.liked = true;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductOptionInfo {
        Long productOptionId;
        String size;
        String color;
        BigDecimal price;
        String productOptionStatus;

        public static ProductOptionInfo from(ProductOption productOption) {
            return new ProductOptionInfo(
                    productOption.getId(),
                    productOption.getSize(),
                    productOption.getColor(),
                    productOption.getPrice(),
                    productOption.getProductOptionStatus().name()
            );
        }
    }
}
