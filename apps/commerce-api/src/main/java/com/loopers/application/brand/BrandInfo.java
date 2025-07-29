package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BrandInfo {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;

    public static BrandInfo from(Brand brand) {
        return new BrandInfo(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getImageUrl()
        );
    }
}
