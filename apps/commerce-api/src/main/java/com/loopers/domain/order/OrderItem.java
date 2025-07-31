package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.ProductOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    private int quantity;

    private BigDecimal orderPrice; // 실제 주문 시점의 가격

    public OrderItem(ProductOption productOption, int quantity, BigDecimal orderPrice) {
        this.productOption = productOption;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    public static OrderItem create(ProductOption productOption, int quantity) {
        return new OrderItem(
                productOption,
                quantity,
                productOption.getPrice()
        );
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    private OrderItem(Order order, ProductOption productOption, int quantity, BigDecimal orderPrice) {
        this.order = order;
        this.productOption = productOption;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    public static OrderItem create(Order order, ProductOption productOption, int quantity, BigDecimal orderPrice) {
        return new OrderItem(order, productOption, quantity, orderPrice);
    }

    public BigDecimal calculateTotalPrice() {
        return orderPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
