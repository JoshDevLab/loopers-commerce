package com.loopers.support.fixture.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCategory;

import java.math.BigDecimal;

public abstract class ProductFixture {

    public static Product createProduct(String name,
                                        String description,
                                        BigDecimal basicPrice,
                                        ProductCategory category,
                                        Brand brand,
                                        String imageUrl) {
        return Product.create(
                name,
                description,
                basicPrice,
                category,
                brand,
                imageUrl
        );
    }


}
