package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ProductServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        Brand brand = brandRepository.save(Brand.create("Brand1", "브랜드 설명", "https://image1.com"));

        productRepository.save(Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        ));

        productRepository.save(Product.create(
                "셔츠2", "상품 설명 2", BigDecimal.valueOf(20000),
                ProductCategory.CLOTHING, brand, "https://image2.jpg"
        ));
    }

    @Test
    @DisplayName("상품 조건 검색 + 페이징 + 정렬 성공")
    void findAllByCriteria() {
        // given
        ProductCriteria criteria = new ProductCriteria(
                "셔츠",
                ProductCategory.CLOTHING,
                null,        // 브랜드 필터 없음
                "latest"     // 정렬 조건
        );
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productService.searchByConditionWithPaging(criteria, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(
                        Product::getName,
                        Product::getDescription,
                        Product::getBasicPrice,
                        Product::getProductCategory,
                        product -> product.getBrand().getName(),
                        Product::getProductStatus
                )
                .containsExactlyInAnyOrder(
                        tuple("셔츠1", "상품 설명 1", BigDecimal.valueOf(10000), ProductCategory.CLOTHING, "Brand1", ProductStatus.ON_SALE),
                        tuple("셔츠2", "상품 설명 2", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, "Brand1", ProductStatus.ON_SALE)

                );
    }
}
