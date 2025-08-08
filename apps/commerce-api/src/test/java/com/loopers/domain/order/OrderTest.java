package com.loopers.domain.order;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 생성 테스트")
class OrderTest {

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        user = User.create("userId", "user@email.com", "1995-01-01", "MALE");
        address = new Address("12345", "도로명주소", "상세주소", "홍길동", "010-1234-5678");
    }

    @Test
    @DisplayName("정상적인 값으로 주문을 생성할 수 있다.")
    void createOrderSuccessfully() {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal discountAmount = new BigDecimal("2000");

        // Act
        Order order = Order.create(user, address, totalAmount, discountAmount);

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getShippingAddress()).isEqualTo(address);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(totalAmount);
        assertThat(order.getDiscountAmount()).isEqualByComparingTo(discountAmount);
        assertThat(order.getPaidAmount()).isEqualByComparingTo(new BigDecimal("8000"));
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("할인 금액이 총 금액보다 크면 INVALID_PAID_AMOUNT 예외가 발생한다.")
    void createOrderWithInvalidPaidAmount() {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("5000");
        BigDecimal discountAmount = new BigDecimal("6000");

        // Act & Assert
        assertThatThrownBy(() -> Order.create(user, address, totalAmount, discountAmount))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_PAID_AMOUNT);
    }

    @DisplayName("할인 금액이 총 금액과 같으면 INVALID_PAID_AMOUNT 예외가 발생한다.")
    @Test
    void createOrderWithEqualTotalAndDiscountAmount() {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal discountAmount = new BigDecimal("10000");

        // Act & Assert
        assertThatThrownBy(() -> Order.create(user, address, totalAmount, discountAmount))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_PAID_AMOUNT);
    }
}

