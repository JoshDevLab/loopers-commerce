package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.OrderInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private BigDecimal paidAmount;

    public static OrderResponse of(OrderInfo orderInfo) {
        return new OrderResponse(orderInfo.getId(), orderInfo.getPaidAmount());
    }
}
