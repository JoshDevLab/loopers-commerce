package com.loopers.interfaces.api.ranking;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.ranking.RankingService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.ranking.dto.RankingResponse;
import com.loopers.support.E2ETestSupport;
import com.loopers.support.RedisZSetOperations;
import com.loopers.support.fixture.brand.BrandFixture;
import com.loopers.support.fixture.product.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class RankingV1ApiE2ETest extends E2ETestSupport {

    private static final String BASE_URL = "/api/v1/rankings";

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    RedisZSetOperations redisZSetOperations;

    @DisplayName("랭킹 조회 API 테스트 - 오늘 날짜")
    @Test
    void getRankings_today() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://image.com"));
        
        Product product1 = productRepository.save(ProductFixture.createProduct(
                "상품1", "설명1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));
        
        Product product2 = productRepository.save(ProductFixture.createProduct(
                "상품2", "설명2", BigDecimal.valueOf(20000),
                ProductCategory.CLOTHING, brand, "https://image2.jpg"
        ));

        // 테스트용 랭킹 데이터 생성
        String rankingKey = "ranking:all:" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.add(rankingKey, product1.getId().toString(), 100.0);
        redisZSetOperations.add(rankingKey, product2.getId().toString(), 90.0);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "?page=0&size=10",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<PageResponse<RankingResponse>>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        
        List<RankingResponse> rankingResponses = response.getBody().data().getContent();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rankingResponses).hasSize(2);
        assertThat(rankingResponses)
                .extracting(
                        RankingResponse::rank,
                        RankingResponse::productId,
                        RankingResponse::productName,
                        RankingResponse::score
                )
                .containsExactly(
                        tuple(1L, product1.getId(), "상품1", 100.0),
                        tuple(2L, product2.getId(), "상품2", 90.0)
                );
    }

    @DisplayName("랭킹 조회 API 테스트 - 특정 날짜")
    @Test
    void getRankings_specificDate() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://image.com"));
        
        Product product = productRepository.save(ProductFixture.createProduct(
                "상품1", "설명1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        LocalDate targetDate = LocalDate.of(2025, 1, 15);
        String rankingKey = "ranking:all:" + targetDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.add(rankingKey, product.getId().toString(), 50.0);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "?date=20250115&page=0&size=10",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<PageResponse<RankingResponse>>>() {}
        );

        // Assert
        List<RankingResponse> rankingResponses = Objects.requireNonNull(response.getBody()).data().getContent();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rankingResponses).hasSize(1);
        assertThat(rankingResponses.get(0).rank()).isEqualTo(1L);
        assertThat(rankingResponses.get(0).productId()).isEqualTo(product.getId());
        assertThat(rankingResponses.get(0).score()).isEqualTo(50.0);
    }

    @DisplayName("랭킹 조회 API 테스트 - 데이터가 없는 경우")
    @Test
    void getRankings_emptyData() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "?page=0&size=10",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<PageResponse<RankingResponse>>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        
        List<RankingResponse> rankingResponses = response.getBody().data().getContent();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rankingResponses).isEmpty();
        assertThat(response.getBody().data().getTotalCount()).isEqualTo(0);
    }

    @DisplayName("랭킹 조회 API 테스트 - 페이징")
    @Test
    void getRankings_paging() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://image.com"));
        
        // 5개 상품 생성
        for (int i = 1; i <= 5; i++) {
            Product product = productRepository.save(ProductFixture.createProduct(
                    "상품" + i, "설명" + i, BigDecimal.valueOf(10000 * i),
                    ProductCategory.CLOTHING, brand, "https://image" + i + ".jpg"
            ));
            
            String rankingKey = "ranking:all:" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            redisZSetOperations.add(rankingKey, product.getId().toString(), 100.0 - i);
        }

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act - 첫 번째 페이지 (size=2)
        var response = client.exchange(
                BASE_URL + "?page=0&size=2",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<PageResponse<RankingResponse>>>() {}
        );

        // Assert
        List<RankingResponse> rankingResponses = Objects.requireNonNull(response.getBody()).data().getContent();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rankingResponses).hasSize(2);
        assertThat(response.getBody().data().getTotalCount()).isEqualTo(5);
        assertThat(response.getBody().data().getTotalPages()).isEqualTo(3);
        
        // 1위, 2위 확인
        assertThat(rankingResponses.get(0).rank()).isEqualTo(1L);
        assertThat(rankingResponses.get(1).rank()).isEqualTo(2L);
    }
}
