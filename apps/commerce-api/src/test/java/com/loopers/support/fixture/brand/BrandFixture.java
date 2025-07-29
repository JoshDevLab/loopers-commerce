package com.loopers.support.fixture.brand;

import com.loopers.domain.brand.Brand;

public abstract class BrandFixture {
    public static Brand createBrand(String name, String description, String imageUrl) {
        return Brand.create(
                name,
                description,
                imageUrl
        );
    }
}
