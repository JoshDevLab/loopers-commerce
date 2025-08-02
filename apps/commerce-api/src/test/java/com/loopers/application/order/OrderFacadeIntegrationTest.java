package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryRepository;
import com.loopers.domain.order.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    OrderFacade orderFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    OrderJpaRepository orderRepository;

    @DisplayName("유효한 주문을 생성할 수 있다.")
    @Test
    void order() {
        // Arrange
        User user = userRepository.save(User.create("userId", "user@email.com", "1995-10-10", "MALE"));
        pointRepository.save(Point.create(BigDecimal.valueOf(200000), user.getId()));

        Brand brand = brandRepository.save(Brand.create("브랜드", "설명", "이미지"));
        Product product1 = productRepository.save(Product.create("상품1", "설명1", BigDecimal.valueOf(20000), ProductCategory.CLOTHING, brand, "img"));
        Product product2 = productRepository.save(Product.create("상품2", "설명2", BigDecimal.valueOf(30000), ProductCategory.CLOTHING, brand, "img"));

        ProductOption productOption1 = productOptionRepository.save(ProductOption.create("옵션1", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(10000), product1));
        ProductOption productOption2 = productOptionRepository.save(ProductOption.create("옵션2", "M", "Blue", ProductStatus.ON_SALE, BigDecimal.valueOf(30000), product2));

        inventoryRepository.save(Inventory.create(productOption1, 10));
        inventoryRepository.save(Inventory.create(productOption2, 5));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(productOption1.getId(), 2),
                new OrderCommand.OrderItemCommand(productOption2.getId(), 1)
        );
        OrderCommand.Register register = new OrderCommand.Register(itemCommands, new Address("zipcode", "roadAddress", "detailAddress", "receiverName", "receiverPhone"));

        // Act
        OrderInfo result = orderFacade.order(register, user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));

        Order savedOrder = orderRepository.findAll().get(0); // 테스트라면 1건만 저장되었을 것이므로

        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(savedOrder.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(savedOrder.getShippingAddress()).isNotNull();
        assertThat(savedOrder.getShippingAddress().getZipcode()).isEqualTo("zipcode");
        assertThat(savedOrder.getShippingAddress().getRoadAddress()).isEqualTo("roadAddress");
        assertThat(savedOrder.getShippingAddress().getDetailAddress()).isEqualTo("detailAddress");

        OrderItem item1 = savedOrder.getOrderItems().get(0);
        OrderItem item2 = savedOrder.getOrderItems().get(1);

        assertThat(item1.getProductOption()).isNotNull();
        assertThat(item1.getQuantity()).isEqualTo(2);
        assertThat(item1.getOrderPrice()).isEqualByComparingTo(item1.getProductOption().getPrice());
        assertThat(item1.calculateTotalPrice()).isEqualByComparingTo(
                item1.getProductOption().getPrice().multiply(BigDecimal.valueOf(item1.getQuantity()))
        );

        assertThat(item2.getProductOption()).isNotNull();
        assertThat(item2.getQuantity()).isEqualTo(1);
        assertThat(item2.getOrderPrice()).isEqualByComparingTo(item2.getProductOption().getPrice());
        assertThat(item2.calculateTotalPrice()).isEqualByComparingTo(
                item2.getProductOption().getPrice().multiply(BigDecimal.valueOf(item2.getQuantity()))
        );
    }

}
