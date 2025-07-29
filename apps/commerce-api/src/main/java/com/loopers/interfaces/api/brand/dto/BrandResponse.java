package com.loopers.interfaces.api.brand.dto;

import com.loopers.application.brand.BrandInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;

    public static BrandResponse from(BrandInfo brandInfo) {
        return new BrandResponse(
                brandInfo.getId(),
                brandInfo.getName(),
                brandInfo.getDescription(),
                brandInfo.getImageUrl()
        );
    }
}
