package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.dto.BrandResponse;
import com.loopers.support.E2ETestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class BrandV1ApiE2ETest extends E2ETestSupport {

    private static final String BASE_URL = "/api/v1/brands";

    @Autowired
    BrandRepository brandRepository;

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

}
