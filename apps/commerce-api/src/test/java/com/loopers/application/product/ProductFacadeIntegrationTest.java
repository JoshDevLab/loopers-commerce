package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ProductFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ProductFacade productFacade;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @BeforeEach
    void setUp() {
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명"));

        productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        productRepository.save(Product.create(
                "크롬하츠 목걸이", "상품 설명 2", BigDecimal.valueOf(20000),
                ProductCategory.ACCESSORY, brand, "https://image2.jpg"
        ));
    }

    @DisplayName("상품 페이지 목록 조회 테스트")
    @Test
    void getProductList() {
        // Arrange
        String keyword = null;
        String category = "clothing";
        Long brandId = null;
        String sortBy = "latest";
        ProductCriteria condition = ProductCriteria.create(
                keyword,
                ProductCategory.valueOfName(category),
                brandId,
                sortBy
        );

        // Act
        Page<ProductInfo> result = productFacade.getProductsWithCondition(condition, PageRequest.of(0, 2));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).extracting(
                ProductInfo::name,
                ProductInfo::description,
                ProductInfo::categoryName,
                ProductInfo::brandName,
                ProductInfo::basicPrice,
                ProductInfo::productStatus
        ).containsExactlyInAnyOrder(
                tuple("셔츠1", "상품 설명 1", "CLOTHING", "Brand1", BigDecimal.valueOf(10000), ProductStatus.ON_SALE)
        );
    }

}
