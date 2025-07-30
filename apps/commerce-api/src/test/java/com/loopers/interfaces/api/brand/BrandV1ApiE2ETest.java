package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCategory;
import com.loopers.domain.product.ProductRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.dto.BrandDetailResponse;
import com.loopers.interfaces.api.brand.dto.BrandResponse;
import com.loopers.interfaces.api.product.dto.ProductResponse;
import com.loopers.support.E2ETestSupport;
import com.loopers.utils.DatabaseCleanUp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class BrandV1ApiE2ETest extends E2ETestSupport {

    private static final String BASE_URL = "/api/v1/brands";

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;


    @BeforeEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("브랜드 목록을 가져올 수 있다.")
    @Test
    void getBrandList() {
        // Arrange
        brandRepository.save(Brand.create("Brand1", "브랜드 설명 1", "https://image1.jpg"));
        brandRepository.save(Brand.create("Brand2", "브랜드 설명 2", "https://image2.jpg"));

        // Act
        ResponseEntity<ApiResponse<List<BrandResponse>>> response = client.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<BrandResponse>>>() {
                }
        );

        // Assert
        List<BrandResponse> result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(
                BrandResponse::getId,
                BrandResponse::getName,
                BrandResponse::getDescription,
                BrandResponse::getImageUrl
        ).containsExactlyInAnyOrder(
                Assertions.tuple(1L, "Brand1", "브랜드 설명 1", "https://image1.jpg"),
                Assertions.tuple(2L, "Brand2", "브랜드 설명 2", "https://image2.jpg")
        );
    }

    @DisplayName("브랜드 상세 정보를 가져올 수 있다.")
    @Test
    void getBrandDetail() {
        // Arrange
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명 1", "https://image1.jpg"));

        Product product1 = productRepository.save(
                Product.create("Product1",
                        "상품 설명 1",
                        BigDecimal.valueOf(10000),
                        ProductCategory.CLOTHING,
                        brand,
                        "https://product1.jpg")
        );

        Product product2 = productRepository.save(
                Product.create("Product2",
                        "상품 설명 2",
                        BigDecimal.valueOf(20000),
                        ProductCategory.CLOTHING,
                        brand,
                        "https://product2.jpg")
        );

        // Act
        ResponseEntity<ApiResponse<BrandDetailResponse>> response = client.exchange(
                BASE_URL + "/" + brand.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<BrandDetailResponse>>() {
                }
        );

        // Assert
        BrandDetailResponse result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(brand.getId());
        assertThat(result.getName()).isEqualTo(brand.getName());
        assertThat(result.getDescription()).isEqualTo(brand.getDescription());
        assertThat(result.getImageUrl()).isEqualTo(brand.getImageUrl());
        assertThat(result.getProducts()).isNotEmpty();
        assertThat(result.getProducts()).extracting(
                        ProductResponse::getName,
                        ProductResponse::getBasicPrice,
                        ProductResponse::getBrandName,
                        ProductResponse::getLikeCount,
                        ProductResponse::getImageUrl,
                        ProductResponse::getCategoryName,
                        ProductResponse::getProductStatus
                )
                .containsExactlyInAnyOrder(
                        tuple(product1.getName(),
                                product1.getBasicPrice().setScale(2),
                                brand.getName(),
                                product1.getLikeCount(),
                                product1.getImageUrl(),
                                product1.getProductCategory().name(),
                                product1.getProductStatus()
                        ),
                        tuple(
                                product2.getName(),
                                product2.getBasicPrice().setScale(2),
                                brand.getName(),
                                product2.getLikeCount(),
                                product2.getImageUrl(),
                                product2.getProductCategory().name(),
                                product2.getProductStatus()
                        )
                );
    }

    @DisplayName("존재하지 않는 브랜드 상세 정보를 요청하면 404 에러가 발생한다.")
    @Test
    void getBrandDetailNotFound() {
        // Act
        ResponseEntity<ApiResponse<BrandDetailResponse>> response = client.exchange(
                BASE_URL + "/999",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<BrandDetailResponse>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
