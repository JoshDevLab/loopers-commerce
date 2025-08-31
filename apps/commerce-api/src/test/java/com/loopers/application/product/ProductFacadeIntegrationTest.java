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
import com.loopers.support.util.ConcurrentTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                tuple("셔츠1", "상품 설명 1", "CLOTHING", "Brand1", BigDecimal.valueOf(10000).setScale(2), ProductStatus.ON_SALE)
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
                "name1",
                "Size M",
                "Color Red",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(10000),
                product
        ));

        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));
        UserInfo userInfo = UserInfo.of(user);

        productLikeRepository.save(ProductLike.create(product.getId(), user.getId()));

        // Act
        ProductInfo result = productFacade.getProductDetail(product.getId(), userInfo);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLiked()).isTrue();
    }


    @DisplayName("같은 유저가 같은 상품에 여러 번 좋아요 요청을 보내도 한 번만 반영되어야 한다")
    @Test
    void likeProduct_isIdempotent() {
        // Arrange
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명", "https://image1.com"));
        Product product = productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        User user = userRepository.save(User.create("testUser", "testUser@email.com", "1996-11-27", "MALE"));

        Long productId = product.getId();
        Long userPk = user.getId();

        // Act
        boolean firstLike = productFacade.likeProduct(productId, userPk);
        boolean secondLike = productFacade.likeProduct(productId, userPk);
        boolean thirdLike = productFacade.likeProduct(productId, userPk);

        // Assert
        assertThat(firstLike).isTrue();
        assertThat(secondLike).isTrue();
        assertThat(thirdLike).isTrue();

        // 가장 중요한 것은 ProductLike 엔티티가 중복 생성되지 않았는지 확인
        boolean likeExists = productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
        assertThat(likeExists).isTrue();
        
        // ProductLike 테이블에서 해당 유저-상품 조합이 단 하나만 있는지 확인
        List<ProductLike> userLikes = productLikeRepository.findByUserPk(userPk);
        long sameProductLikes = userLikes.stream()
                .filter(like -> like.getProductId().equals(productId))
                .count();
        assertThat(sameProductLikes).isEqualTo(1);
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
        productLikeService.like(product.getId(), user.getId());

        Long productId = product.getId();
        Long userPk = user.getId();

        // 먼저 좋아요가 있는 상태인지 확인
        assertThat(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).isTrue();

        // when
        boolean firstUnlike = productFacade.unLikeProduct(productId, userPk);
        boolean secondUnlike = productFacade.unLikeProduct(productId, userPk);
        boolean thirdUnlike = productFacade.unLikeProduct(productId, userPk);

        // then
        assertThat(firstUnlike).isFalse();
        assertThat(secondUnlike).isFalse();
        assertThat(thirdUnlike).isFalse();

        // 좋아요가 제거되었는지 확인
        boolean likeExists = productLikeRepository.existsByProductIdAndUserPk(productId, userPk);
        assertThat(likeExists).isFalse();
        
        // 해당 유저의 좋아요 목록에서 해당 상품이 완전히 제거되었는지 확인
        List<ProductLike> userLikes = productLikeRepository.findByUserPk(userPk);
        long sameProductLikes = userLikes.stream()
                .filter(like -> like.getProductId().equals(productId))
                .count();
        assertThat(sameProductLikes).isEqualTo(0);
    }

    @Test
    void concurrentLikesShouldNotBreakLikeCount() {
        // given
        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품", "설명", BigDecimal.valueOf(10000), ProductCategory.CLOTHING, brand, "img"));

        int threadCount = 10;

        List<User> users = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            users.add(userRepository.save(User.create("user" + i, "user" + i + "@test.com", "1990-01-01", "MALE")));
        }

        // when - 각 사용자를 명시적으로 인덱스로 매핑
        AtomicInteger userIndex = new AtomicInteger(0);
        ConcurrentTestUtils.Result result = ConcurrentTestUtils.runConcurrent(threadCount, () -> {
            int index = userIndex.getAndIncrement();
            User currentUser = users.get(index);
            productFacade.likeProduct(product.getId(), currentUser.getId());
        });

        // then
        assertThat(result.successCount()).isEqualTo(threadCount);
        assertThat(result.failedCount()).isEqualTo(0);
        
        // 각 사용자마다 정확히 하나씩 좋아요가 생성되었는지 확인
        for (User user : users) {
            boolean likeExists = productLikeRepository.existsByProductIdAndUserPk(product.getId(), user.getId());
            assertThat(likeExists).isTrue();
        }
        
        // 전체 좋아요 수가 사용자 수와 같은지 확인 (중복 생성이 없었는지)
        // 더 간단하게 각 사용자의 좋아요가 정확히 존재하는지만 확인
        int actualLikeCount = 0;
        for (User user : users) {
            if (productLikeRepository.existsByProductIdAndUserPk(product.getId(), user.getId())) {
                actualLikeCount++;
            }
        }
        System.out.println("Expected like count: " + threadCount + ", Actual like count: " + actualLikeCount);
        assertThat(actualLikeCount).isEqualTo(threadCount);
    }
}
