package com.loopers.interfaces.api.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCategory;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.LikedResponse;
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
import static org.assertj.core.groups.Tuple.tuple;

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

    @DisplayName("사용자는 상품에 대하여 좋아요를 하면 201 CREATED 응답 코드와 상품좋아요 여부 및 상품의 좋아요 수 를 반환한다.")
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
                new ParameterizedTypeReference<ApiResponse<LikedResponse>>() {
                }
        );

        // Assert
        boolean existsByProductAndUser = productLikeRepository.existsByProductAndUser(product, user);
        LikedResponse result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getLikeCount()).isEqualTo(1);
        assertThat(result.isLiked()).isTrue();
        assertThat(existsByProductAndUser).isTrue();
    }

    @DisplayName("사용자는 상품에 대하여 좋아요를 취소하면 200 OK 응답 코드와 상품좋아요 여부 및 상품의 좋아요 수 를 반환한다.")
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
                new ParameterizedTypeReference<ApiResponse<LikedResponse>>() {
                }
        );

        // Act
        var response = client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.DELETE,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<LikedResponse>>() {
                }
        );

        // Assert
        boolean existsByProductAndUser = productLikeRepository.existsByProductAndUser(product, user);
        LikedResponse result = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getLikeCount()).isEqualTo(0);
        assertThat(result.isLiked()).isFalse();
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

        client.exchange(
                BASE_URL + "/" + product1.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<LikedResponse>>() {
                }
        );

        client.exchange(
                BASE_URL + "/" + product2.getId(),
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<LikedResponse>>() {
                }
        );

        // Act
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
        assertThat(result.size()).isEqualTo(2);
        assertThat(result)
                .extracting(
                        ProductResponse::getName,
                        ProductResponse::getBasicPrice,
                        ProductResponse::getBrandName,
                        ProductResponse::getLikeCount,
                        ProductResponse::getLiked,
                        ProductResponse::getImageUrl,
                        ProductResponse::getCategoryName,
                        ProductResponse::getProductStatus
                )
                .containsExactlyInAnyOrder(
                        tuple("testProduct1",
                                BigDecimal.valueOf(10000).setScale(2),
                                brand.getName(),
                                1,
                                true,
                                "https://example.com/image1.jpg",
                                ProductCategory.CLOTHING.name(),
                                ProductStatus.ON_SALE
                        ),
                        tuple("testProduct2",
                                BigDecimal.valueOf(20000).setScale(2),
                                brand.getName(),
                                1,
                                true,
                                "https://example.com/image2.jpg",
                                ProductCategory.ACCESSORY.name(),
                                ProductStatus.ON_SALE
                        )
                );
    }
}
