package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.OrderItemInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private Long productOptionId;
    private String productOptionName;
    private BigDecimal price;
    private int quantity;
    private String size;
    private String color;

    public static OrderItemResponse of(OrderItemInfo orderItemInfo) {
        return new OrderItemResponse(
                orderItemInfo.getProductId(),
                orderItemInfo.getProductOptionId(),
                orderItemInfo.getProductOptionName(),
                orderItemInfo.getPrice(),
                orderItemInfo.getQuantity(),
                orderItemInfo.getSize(),
                orderItemInfo.getColor()
        );
    }
}
