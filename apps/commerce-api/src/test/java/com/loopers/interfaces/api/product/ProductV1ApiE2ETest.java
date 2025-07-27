package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductCategory;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.dto.ProductResponse;
import com.loopers.support.E2ETestSupport;
import com.loopers.support.fixture.brand.BrandFixture;
import com.loopers.support.fixture.product.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ProductV1ApiE2ETest extends E2ETestSupport {

    private static final String BASE_URL = "/api/v1/products";

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @BeforeEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품목록 조회 API 테스트")
    @Test
    void getProductList() {
        // Arrange
        Brand brand1 = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1"));
        Brand brand2 = brandRepository.save(BrandFixture.createBrand("Brand2", "Brand description 2"));
        productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand1,
                "https://example.com/image1.jpg"
        ));
        productRepository.save(ProductFixture.createProduct(
                "testProduct2",
                "Description for product 2",
                BigDecimal.valueOf(50000),
                ProductCategory.CLOTHING,
                brand2,
                "https://example.com/image2.jpg"
        ));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "?page=0&size=20&sort=latest&category=CLOTHING&brand=Brand1",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<PageResponse<ProductResponse>>>() {
                }
        );

        // Assert
        List<ProductResponse> productResponses = Objects.requireNonNull(response.getBody()).data().getContent();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productResponses).hasSize(2);
        assertThat(productResponses)
                .extracting(
                        ProductResponse::name,
                        ProductResponse::basicPrice,
                        ProductResponse::brandName,
                        ProductResponse::likeCount,
                        ProductResponse::imageUrl,
                        ProductResponse::categoryName,
                        ProductResponse::productStatus
                )
                .containsExactlyInAnyOrder(
                        tuple("testProduct1",
                                BigDecimal.valueOf(10000).setScale(2),
                                brand1.getName(),
                                0,
                                "https://example.com/image1.jpg",
                                ProductCategory.CLOTHING.name(),
                                ProductStatus.ON_SALE
                        ),
                        tuple("testProduct2",
                                BigDecimal.valueOf(50000).setScale(2),
                                brand2.getName(),
                                0,
                                "https://example.com/image2.jpg",
                                ProductCategory.CLOTHING.name(),
                                ProductStatus.ON_SALE
                        )
                );

    }
}
