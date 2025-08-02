package com.loopers.interfaces.api.brand.dto;

import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.product.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BrandDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private List<ProductResponse> products;

    public static BrandDetailResponse from(BrandInfo brandInfo) {
        return new BrandDetailResponse(
                brandInfo.getId(),
                brandInfo.getName(),
                brandInfo.getDescription(),
                brandInfo.getImageUrl(),
                brandInfo.getProducts().stream()
                        .map(ProductResponse::from)
                        .toList()
        );
    }
}
