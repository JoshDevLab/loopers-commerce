package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
@Entity
public class Product extends BaseEntity {

    private String name;

    private String description;

    private BigDecimal basicPrice;

    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory;

    private int likeCount;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private String imageUrl;

    private Product(String name,
                   String description,
                   BigDecimal basicPrice,
                   ProductCategory productCategory,
                   int likeCount,
                   ProductStatus productStatus,
                   Brand brand,
                   String imageUrl) {
        this.name = name;
        this.description = description;
        this.basicPrice = basicPrice;
        this.productCategory = productCategory;
        this.likeCount = likeCount;
        this.productStatus = productStatus;
        this.brand = brand;
        this.imageUrl = imageUrl;
    }

    public static Product create(String name, String description, BigDecimal basicPrice,
                                 ProductCategory productCategory, Brand brand, String imageUrl) {

        return new Product(
                name,
                description,
                basicPrice,
                productCategory,
                0, // 초기 좋아요 수는 0
                ProductStatus.ON_SALE, // 초기 상태는 판매 중
                brand,
                imageUrl
        );

    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        if (this.likeCount-1 >= 0) {
            this.likeCount -= 1;
        }
    }
}
