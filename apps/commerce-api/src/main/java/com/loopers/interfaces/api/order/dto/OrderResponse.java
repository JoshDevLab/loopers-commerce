package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.OrderInfo;
import com.loopers.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private BigDecimal paidAmount;

    public static OrderResponse of(OrderInfo orderInfo) {
        return new OrderResponse(orderInfo.getId(), orderInfo.getPaidAmount());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryResponse {
        private Long orderId;
        private BigDecimal totalAmount;
        private OrderStatus status;
        private ZonedDateTime createdAt;

        public static OrderSummaryResponse of(OrderInfo orderInfo) {
            return new OrderSummaryResponse(orderInfo.getId(), orderInfo.getPaidAmount(), orderInfo.getStatus(), orderInfo.getCreatedAt());
        }
    }
}
