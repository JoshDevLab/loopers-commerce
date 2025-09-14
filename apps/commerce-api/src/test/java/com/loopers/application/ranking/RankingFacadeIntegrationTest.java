package com.loopers.application.ranking;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCategory;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.IntegrationTestSupport;
import com.loopers.support.RedisZSetOperations;
import com.loopers.support.fixture.brand.BrandFixture;
import com.loopers.support.fixture.product.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class RankingFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    RankingFacade rankingFacade;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    RankingService rankingService;

    @Autowired
    RedisZSetOperations redisZSetOperations;

    @DisplayName("랭킹 조회 시 상품 정보와 함께 반환한다")
    @Test
    void getRankings_withProductInfo() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://brand.jpg"));
        
        Product product1 = productRepository.save(ProductFixture.createProduct(
                "인기상품1", "인기상품 설명1", BigDecimal.valueOf(15000),
                ProductCategory.CLOTHING, brand, "https://product1.jpg"
        ));
        
        Product product2 = productRepository.save(ProductFixture.createProduct(
                "인기상품2", "인기상품 설명2", BigDecimal.valueOf(25000),
                ProductCategory.ACCESSORY, brand, "https://product2.jpg"
        ));

        LocalDate today = LocalDate.now();
        String rankingKey = "ranking:all:" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.add(rankingKey, product1.getId().toString(), 150.0);
        redisZSetOperations.add(rankingKey, product2.getId().toString(), 120.0);

        // Act
        Page<RankingInfo> result = rankingFacade.getRankings(today, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(
                        RankingInfo::rank,
                        RankingInfo::productId,
                        RankingInfo::productName,
                        RankingInfo::imageUrl,
                        RankingInfo::price,
                        RankingInfo::score
                )
                .containsExactly(
                        tuple(1L, product1.getId(), "인기상품1", "https://product1.jpg", BigDecimal.valueOf(15000).setScale(2), 150.0),
                        tuple(2L, product2.getId(), "인기상품2", "https://product2.jpg", BigDecimal.valueOf(25000).setScale(2), 120.0)
                );
    }

    @DisplayName("날짜가 null이면 오늘 날짜로 랭킹을 조회한다")
    @Test
    void getRankings_nullDate_usesToday() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://brand.jpg"));
        
        Product product = productRepository.save(ProductFixture.createProduct(
                "오늘의 상품", "오늘의 상품 설명", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://today.jpg"
        ));

        LocalDate today = LocalDate.now();
        String rankingKey = "ranking:all:" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.add(rankingKey, product.getId().toString(), 100.0);

        // Act
        Page<RankingInfo> result = rankingFacade.getRankings(null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).productName()).isEqualTo("오늘의 상품");
    }

    @DisplayName("랭킹에 없는 상품ID가 있어도 에러없이 처리한다")
    @Test
    void getRankings_withDeletedProduct() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://brand.jpg"));
        
        Product existProduct = productRepository.save(ProductFixture.createProduct(
                "존재하는 상품", "존재하는 상품 설명", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://exist.jpg"
        ));

        LocalDate today = LocalDate.now();
        String rankingKey = "ranking:all:" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisZSetOperations.add(rankingKey, existProduct.getId().toString(), 100.0);
        redisZSetOperations.add(rankingKey, "999", 90.0); // 존재하지 않는 상품ID

        // Act
        Page<RankingInfo> result = rankingFacade.getRankings(today, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).productName()).isEqualTo("존재하는 상품");
        assertThat(result.getContent().get(1).productName()).isEqualTo("상품명 없음");
        assertThat(result.getContent().get(1).imageUrl()).isNull();
        assertThat(result.getContent().get(1).price()).isNull();
    }

    @DisplayName("빈 랭킹 데이터는 빈 페이지를 반환한다")
    @Test
    void getRankings_emptyData() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        Page<RankingInfo> result = rankingFacade.getRankings(today, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @DisplayName("페이징이 정확히 작동한다")
    @Test
    void getRankings_paging() {
        // Arrange
        Brand brand = brandRepository.save(BrandFixture.createBrand("TestBrand", "Test Description", "https://brand.jpg"));
        
        // 10개 상품 생성 및 랭킹 설정
        LocalDate today = LocalDate.now();
        String rankingKey = "ranking:all:" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        for (int i = 1; i <= 10; i++) {
            Product product = productRepository.save(ProductFixture.createProduct(
                    "상품" + i, "상품 설명" + i, BigDecimal.valueOf(1000 * i),
                    ProductCategory.CLOTHING, brand, "https://product" + i + ".jpg"
            ));
            redisZSetOperations.add(rankingKey, product.getId().toString(), 200.0 - i); // 점수 내림차순
        }

        // Act - 두 번째 페이지 (5~8위)
        Page<RankingInfo> result = rankingFacade.getRankings(today, PageRequest.of(1, 4));

        // Assert
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1); // 현재 페이지
        
        // 5위부터 8위까지 확인
        assertThat(result.getContent())
                .extracting(RankingInfo::rank)
                .containsExactly(5L, 6L, 7L, 8L);
    }
}
