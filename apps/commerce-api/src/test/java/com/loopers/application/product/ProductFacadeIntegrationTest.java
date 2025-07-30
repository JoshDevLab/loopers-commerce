package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ProductFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ProductFacade productFacade;

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

    @Autowired
    ProductLikeService productLikeService;


    @DisplayName("상품 페이지 목록 조회 테스트")
    @Test
    void getProductList() {
        // Arrange
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명", "https://image1.com"));

        productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        productRepository.save(Product.create(
                "크롬하츠 목걸이", "상품 설명 2", BigDecimal.valueOf(20000),
                ProductCategory.ACCESSORY, brand, "https://image2.jpg"
        ));

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
                ProductInfo::getName,
                ProductInfo::getDescription,
                ProductInfo::getCategoryName,
                ProductInfo::getBrandName,
                ProductInfo::getBasicPrice,
                ProductInfo::getProductStatus
        ).containsExactlyInAnyOrder(
                tuple("셔츠1", "상품 설명 1", "CLOTHING", "Brand1", BigDecimal.valueOf(10000), ProductStatus.ON_SALE)
        );
    }

    @Test
    @DisplayName("로그인한 사용자가 좋아요한 상품을 상세조회하면 liked가 true로 표시된다")
    void getProductDetailWithLoginAndLiked() {
        // Arrange
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명", "https://image1.com"));

        Product product = productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        productOptionRepository.save(ProductOption.create(
                "Size M",
                "Color Red",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(10000),
                product
        ));

        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        UserInfo userInfo = UserInfo.of(user);

        productLikeRepository.save(ProductLike.create(product, user));

        // when
        ProductInfo result = productFacade.getProductDetail(product.getId(), userInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLiked()).isTrue();
    }


    @DisplayName("같은 유저가 같은 상품에 여러 번 좋아요 요청을 보내도 한 번만 반영되어야 한다")
    @Test
    void likeProduct_isIdempotent() {
        // given
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명", "https://image1.com"));
        Product product = productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        Long productId = product.getId();
        Long userPk = user.getId();

        // when
        ProductLikedInfo firstLike = productFacade.likeProduct(productId, userPk);
        ProductLikedInfo secondLike = productFacade.likeProduct(productId, userPk);
        ProductLikedInfo thirdLike = productFacade.likeProduct(productId, userPk);

        // then
        assertThat(firstLike.isLiked()).isTrue();
        assertThat(secondLike.isLiked()).isTrue();
        assertThat(thirdLike.isLiked()).isTrue();

        // 좋아요 수는 한 번만 증가해야 함
        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertThat(updatedProduct.get().getLikeCount()).isEqualTo(1);
    }

    @DisplayName("같은 유저가 같은 상품에 여러 번 좋아요 취소 요청을 보내도 한 번만 반영되어야 한다")
    @Test
    void unLikeProduct_isIdempotent() {
        // given
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명", "https://image1.com"));
        Product product = productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));
        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        productLikeService.like(product, user);

        Long productId = product.getId();
        Long userPk = user.getId();

        // when
        ProductLikedInfo firstLike = productFacade.unLikeProduct(productId, userPk);
        ProductLikedInfo secondLike = productFacade.unLikeProduct(productId, userPk);
        ProductLikedInfo thirdLike = productFacade.unLikeProduct(productId, userPk);

        // then
        assertThat(firstLike.isLiked()).isFalse();
        assertThat(secondLike.isLiked()).isFalse();
        assertThat(thirdLike.isLiked()).isFalse();

        // 좋아요 수는 한 번만 증가해야 함
        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertThat(updatedProduct.get().getLikeCount()).isEqualTo(0);
    }
}
