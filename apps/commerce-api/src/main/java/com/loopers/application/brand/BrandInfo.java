package com.loopers.application.brand;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BrandInfo {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private List<ProductInfo> products;

    private BrandInfo(Long id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public static BrandInfo from(Brand brand) {
        return new BrandInfo(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getImageUrl()
        );
    }

    public static BrandInfo from(Brand brand, List<Product> products) {
        return new BrandInfo(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getImageUrl(),
                products.stream().map(ProductInfo::from).toList()
        );
    }
}
