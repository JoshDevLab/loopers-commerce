package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductTest {

    @Test
    @DisplayName("좋아요 수 증가 테스트")
    void increaseLikeCount() {
        // given
        Product product = getProduct();

        // when
        product.increaseLikeCount();

        // then
        assertThat(product.getLikeCount()).isEqualTo(1);
    }


    @Test
    @DisplayName("좋아요 수 감소 테스트")
    void decreaseLikeCount() {
        // given
        Product product = getProduct();
        product.increaseLikeCount();
        product.increaseLikeCount();

        // when
        product.decreaseLikeCount();

        // then
        assertThat(product.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 수는 0 이하로 내려가지 않는다")
    void decreaseLikeCount_minimumIsZero() {
        // given
        Product product = getProduct();

        // when
        product.decreaseLikeCount();

        // then
        assertThat(product.getLikeCount()).isZero();
    }


    private static Product getProduct() {
        Brand brand = Brand.create("Brand1", "브랜드 설명", "https://image1.com");
        return Product.create(
                "셔츠1", "상품 설명 1", BigDecimal.valueOf(10000),
                ProductCategory.CLOTHING, brand, "https://image1.jpg"
        );
    }

}
