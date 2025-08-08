package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryRepository;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.*;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemProcessorIntegrationTest extends IntegrationTestSupport {

    @Autowired
    OrderItemProcessor orderItemProcessor;

    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;

    @DisplayName("주문할 상품옵션 들의 재고를 차감하고 totalAmount, orderitems, 재고 히스토리를 반환한다.")
    @Test
    void orderItemProcessDecreaseQuantityThenReturn_orderItems_totalAmount_inventoryHistories() {
        // Arrange
        Brand brand = brandRepository.save(Brand.create("브랜드", "desc", "image"));
        Product product = productRepository.save(Product.create("상품", "desc", BigDecimal.valueOf(10000), ProductCategory.CLOTHING, brand, "img"));
        ProductOption option = productOptionRepository.save(ProductOption.create("옵션", "L", "Red", ProductStatus.ON_SALE, BigDecimal.valueOf(5000), product));
        inventoryRepository.save(Inventory.create(option, 10));

        List<OrderCommand.OrderItemCommand> itemCommands = List.of(
                new OrderCommand.OrderItemCommand(option.getId(), 2)
        );

        // Act
        OrderItemProcessor.Result result = orderItemProcessor.process(itemCommands);

        // Assert
        assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(result.orderItems()).hasSize(1);
        assertThat(result.inventoryHistories()).hasSize(1);
    }

}
