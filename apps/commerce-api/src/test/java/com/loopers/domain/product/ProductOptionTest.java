package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductOptionTest {
    @DisplayName("상품 옵션 상태가 ON_SALE이면 예외 없이 통과한다")
    @Test
    void isOnSales_success() {
        // Arrange
        ProductOption option = ProductOption.create(
                "옵션명", "L", "Blue",
                ProductStatus.ON_SALE,
                BigDecimal.valueOf(10000),
                null
        );

        // Act
        // Assert
        assertThatCode(option::isOnSales).doesNotThrowAnyException();
    }

    @DisplayName("상품 옵션 상태가 ON_SALE이 아니면 예외가 발생한다")
    @Test
    void isOnSales_fail() {
        // Arrange
        ProductOption option = ProductOption.create(
                "옵션명", "L", "Blue",
                ProductStatus.SOLD_OUT,
                BigDecimal.valueOf(10000),
                null
        );

        // Act
        // Assert
        assertThatThrownBy(option::isOnSales)
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("는 판매중인 상품이 아닙니다.")
                .extracting("errorType")
                .isEqualTo(ErrorType.PRODUCT_OPTION_NOT_ON_SALE);
    }
}
