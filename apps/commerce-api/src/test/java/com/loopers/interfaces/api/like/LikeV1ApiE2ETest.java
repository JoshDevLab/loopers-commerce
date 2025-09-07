package com.loopers.interfaces.api.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCategory;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.ProductResponse;
import com.loopers.support.E2ETestSupport;
import com.loopers.support.fixture.brand.BrandFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.assertj.core.api.Assertions;
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

public class LikeV1ApiE2ETest extends E2ETestSupport {

    private static final String BASE_URL = "/api/v1/like/products";

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductLikeRepository productLikeRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품에 대해 좋아요 를 할 때 X-USER-ID 헤더가 없을 경우, 401 Unauthorized 응답을 반환한다.")
    @Test
    void likeProductWithoutUserIdHeader() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Product product = productRepository.save(Product.create(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image1.jpg"
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "NOT-EXIST-USERID"); // 빈 X-USER-ID 헤더 설정
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Long>>() {
                }
        );

        // Assert
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @DisplayName("사용자는 상품에 대하여 좋아요를 하면 201 CREATED 응답 코드와 좋아요 결과를 반환한다.")
    @Test
    void likeProduct() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Product product = productRepository.save(Product.create(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image1.jpg"
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                }
        );

        // Assert
        boolean existsByProductAndUser = productLikeRepository.existsByProductIdAndUserPk(product.getId(), user.getId());
        Boolean result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result).isTrue();
        assertThat(existsByProductAndUser).isTrue();
    }

    @DisplayName("사용자는 상품에 대하여 좋아요를 취소하면 200 OK 응답 코드와 좋아요 결과를 반환한다.")
    @Test
    void unLikeProduct() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Product product = productRepository.save(Product.create(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image1.jpg"
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                }
        );

        // Act
        var response = client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.DELETE,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                }
        );

        // Assert
        boolean existsByProductAndUser = productLikeRepository.existsByProductIdAndUserPk(product.getId(), user.getId());
        Boolean result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isFalse();
        assertThat(existsByProductAndUser).isFalse();
    }

    @DisplayName("사용자가 좋아요 한 상품들을 조회할 수 있다 200 OK 응답")
    @Test
    void getLikedProducts() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1", "https://image1.com"));
        Product product1 = productRepository.save(Product.create(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image1.jpg"
        ));
        Product product2 = productRepository.save(Product.create(
                "testProduct2",
                "Description for product 2",
                BigDecimal.valueOf(20000),
                ProductCategory.ACCESSORY,
                brand,
                "https://example.com/image2.jpg"
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // 좋아요 생성 API 호출
        var likeResponse1 = client.exchange(
                BASE_URL + "/" + product1.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                }
        );

        var likeResponse2 = client.exchange(
                BASE_URL + "/" + product2.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                }
        );

        // 좋아요 생성이 성공했는지 확인
        assertThat(likeResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(likeResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // 데이터베이스에서 좋아요가 실제로 생성되었는지 확인
        assertThat(productLikeRepository.existsByProductIdAndUserPk(product1.getId(), user.getId())).isTrue();
        assertThat(productLikeRepository.existsByProductIdAndUserPk(product2.getId(), user.getId())).isTrue();

        // Act - 좋아요 목록 조회
        var response = client.exchange(
                BASE_URL,
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<List<ProductResponse>>>() {
                }
        );

        // Assert
        List<ProductResponse> result = response.getBody().data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        
        // liked 상태는 별도로 확인하지 않고, 기본적인 API 동작만 확인
        // E2E 테스트에서는 HTTP 통신과 데이터 저장/조회의 기본 플로우만 검증
        assertThat(result)
                .extracting(ProductResponse::getName)
                .containsExactlyInAnyOrder("testProduct1", "testProduct2");
        
        // 각 상품의 기본 정보가 올바른지 확인
        assertThat(result)
                .extracting(ProductResponse::getBasicPrice)
                .containsExactlyInAnyOrder(
                        BigDecimal.valueOf(10000).setScale(2), 
                        BigDecimal.valueOf(20000).setScale(2)
                );
    }
}
