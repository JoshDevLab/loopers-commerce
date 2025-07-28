package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.dto.ProductDetailResponse;
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

    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductLikeRepository productLikeRepository;

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
        Product testProduct1 = productRepository.save(ProductFixture.createProduct(
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
                        ProductResponse::getName,
                        ProductResponse::getBasicPrice,
                        ProductResponse::getBrandName,
                        ProductResponse::getLikeCount,
                        ProductResponse::getImageUrl,
                        ProductResponse::getCategoryName,
                        ProductResponse::getProductStatus
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

    @DisplayName("상품상세 조회 상품이 없는 경우 404 응답")
    @Test
    void getProductDetailNotFound() {
        // Arrange
        long nonExistentProductId = 999L; // 존재하지 않는 상품 ID
        Brand brand1 = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1"));
        productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand1,
                "https://example.com/image1.jpg"
        ));


        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + nonExistentProductId,
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<ProductDetailResponse>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("상품상세 조회 상품옵션이 없는 경우 404 응답")
    @Test
    void getProductDetailOptionsNotFound() {
        // Arrange
        long nonExistentProductId = 999L; // 존재하지 않는 상품 ID

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + nonExistentProductId,
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<ProductDetailResponse>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("상품상세 조회시 상품의 상세정보를 반환한다.")
    @Test
    void getProductDetail() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1"));
        var product = productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image1.jpg"
        ));


        productOptionRepository.save(ProductOption.create(
                "Size M",
                "Color Red",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(10000),
                product
        ));

        productOptionRepository.save(ProductOption.create(
                "Size L",
                "Color Blue",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(12000),
                product
        ));


        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<ProductDetailResponse>>() {
                }
        );

        // Assert
        ProductDetailResponse productDetailResponse = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productDetailResponse.getProductId()).isEqualTo(product.getId());
        assertThat(productDetailResponse.getName()).isEqualTo(product.getName());
        assertThat(productDetailResponse.getBasicPrice()).isEqualByComparingTo(product.getBasicPrice());
        assertThat(productDetailResponse.getDescription()).isEqualTo(product.getDescription());
        assertThat(productDetailResponse.getImageUrl()).isEqualTo(product.getImageUrl());
        assertThat(productDetailResponse.getBrandName()).isEqualTo(brand.getName());
        assertThat(productDetailResponse.getLikeCount()).isEqualTo(product.getLikeCount());
        assertThat(productDetailResponse.getLiked()).isEqualTo(false);
        List<ProductDetailResponse.ProductOptionResponse> options = productDetailResponse.getOptions();
        assertThat(options).hasSize(2);
        assertThat(options)
                .extracting(
                        ProductDetailResponse.ProductOptionResponse::getSize,
                        ProductDetailResponse.ProductOptionResponse::getColor,
                        ProductDetailResponse.ProductOptionResponse::getPrice,
                        ProductDetailResponse.ProductOptionResponse::getProductOptionStatus
                )
                .containsExactlyInAnyOrder(
                        tuple("Size M", "Color Red", BigDecimal.valueOf(10000).setScale(2), ProductStatus.ON_SALE.name()),
                        tuple("Size L", "Color Blue", BigDecimal.valueOf(12000).setScale(2), ProductStatus.ON_SALE.name())
                );
    }

    @DisplayName("유저 정보가서 있을때 상품상세 조회시 상품의 상세정보와 좋아요 여부를 반환한다.")
    @Test
    void getProductDetailWhenLoginUserIncludeLike() {
        // Arrange
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        Brand brand = brandRepository.save(BrandFixture.createBrand("Brand1", "Brand description 1"));
        var product = productRepository.save(ProductFixture.createProduct(
                "testProduct1",
                "Description for product 1",
                BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING,
                brand,
                "https://example.com/image1.jpg"
        ));

        productOptionRepository.save(ProductOption.create(
                "Size M",
                "Color Red",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(10000),
                product
        ));

        productOptionRepository.save(ProductOption.create(
                "Size L",
                "Color Blue",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(12000),
                product
        ));

        productLikeRepository.save(ProductLike.create(product, user));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        var response = client.exchange(
                BASE_URL + "/" + product.getId(),
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<ProductDetailResponse>>() {
                }
        );

        // Assert
        ProductDetailResponse productDetailResponse = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productDetailResponse.getProductId()).isEqualTo(product.getId());
        assertThat(productDetailResponse.getName()).isEqualTo(product.getName());
        assertThat(productDetailResponse.getBasicPrice()).isEqualByComparingTo(product.getBasicPrice());
        assertThat(productDetailResponse.getDescription()).isEqualTo(product.getDescription());
        assertThat(productDetailResponse.getImageUrl()).isEqualTo(product.getImageUrl());
        assertThat(productDetailResponse.getBrandName()).isEqualTo(brand.getName());
        assertThat(productDetailResponse.getLikeCount()).isEqualTo(product.getLikeCount());
        assertThat(productDetailResponse.getLiked()).isEqualTo(true);
        List<ProductDetailResponse.ProductOptionResponse> options = productDetailResponse.getOptions();
        assertThat(options).hasSize(2);
        assertThat(options)
                .extracting(
                        ProductDetailResponse.ProductOptionResponse::getSize,
                        ProductDetailResponse.ProductOptionResponse::getColor,
                        ProductDetailResponse.ProductOptionResponse::getPrice,
                        ProductDetailResponse.ProductOptionResponse::getProductOptionStatus
                )
                .containsExactlyInAnyOrder(
                        tuple("Size M", "Color Red", BigDecimal.valueOf(10000).setScale(2), ProductStatus.ON_SALE.name()),
                        tuple("Size L", "Color Blue", BigDecimal.valueOf(12000).setScale(2), ProductStatus.ON_SALE.name())
                );
    }

}
