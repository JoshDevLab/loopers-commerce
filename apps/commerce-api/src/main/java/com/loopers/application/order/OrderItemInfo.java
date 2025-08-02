package com.loopers.application.order;

import com.loopers.domain.order.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemInfo {
    private Long productId;
    private Long productOptionId;
    private String productOptionName;
    private BigDecimal price;
    private int quantity;
    private String size;
    private String color;

    public static OrderItemInfo from(OrderItem orderItem) {
        return new OrderItemInfo(
                orderItem.getProductOption().getProduct().getId(),
                orderItem.getProductOption().getId(),
                orderItem.getProductOption().getName(),
                orderItem.getProductOption().getPrice(),
                orderItem.getQuantity(),
                orderItem.getProductOption().getSize(),
                orderItem.getProductOption().getColor()
        );
    }
}
