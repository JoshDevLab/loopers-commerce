package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.Address;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatorIntegrationTest extends IntegrationTestSupport {
    @Autowired
    OrderCreator orderCreator;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductOptionRepository productOptionRepository;

    @DisplayName("주문을 생성하여 반환한다")
    @Test
    void createOrder() {
        // Arrange
        User user = userRepository.save(User.create("userId", "email@email.com", "1990-01-01", "MALE"));
        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption productOption = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product));

        List<OrderItem> orderItems = List.of(OrderItem.create(productOption, 2));
        Address address = new Address("zip", "road", "detail", "name", "010-0000-0000");
        BigDecimal totalAmount = BigDecimal.valueOf(20000);
        BigDecimal discountAmount = BigDecimal.valueOf(5000);

        // Act
        Order order = orderCreator.createOrder(user.getId(), orderItems, address, totalAmount, discountAmount);

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getShippingAddress()).isEqualTo(address);
    }

}
