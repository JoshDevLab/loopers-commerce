package com.loopers.domain.inventory;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class InventoryTest {

    @DisplayName("재고가 충분하면 예외가 발생하지 않는다")
    @Test
    void hasEnoughQuantity_success() {
        // Arrange
        Inventory inventory = Inventory.create(null, 10);

        // Act
        // Assert
        assertThatCode(() -> inventory.hasEnoughQuantity(5))
                .doesNotThrowAnyException();
    }

    @DisplayName("재고가 부족하면 예외가 발생한다")
    @Test
    void hasEnoughQuantity_insufficient() {
        // Arrange
        Inventory inventory = Inventory.create(null, 3);

        // Act
        // Assert
        assertThatThrownBy(() -> inventory.hasEnoughQuantity(5))
                .isInstanceOf(CoreException.class)
                .hasMessage("상품 재고가 없습니다.")
                .extracting("errorType")
                .isEqualTo(ErrorType.INSUFFICIENT_STOCK);
    }

    @DisplayName("재고가 차감된다")
    @Test
    void decreaseQuantity_success() {
        // Arrange
        Inventory inventory = Inventory.create(null, 10);

        // Act
        inventory.decreaseQuantity(4);

        // Assert
        assertThat(inventory.getQuantity()).isEqualTo(6);
    }
}
