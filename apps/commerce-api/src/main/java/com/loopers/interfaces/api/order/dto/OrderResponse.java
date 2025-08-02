package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.OrderDetailInfo;
import com.loopers.application.order.OrderInfo;
import com.loopers.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

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
    public static class Summary {
        private Long orderId;
        private BigDecimal totalAmount;
        private OrderStatus status;
        private ZonedDateTime createdAt;

        public static Summary of(OrderInfo orderInfo) {
            return new Summary(orderInfo.getId(), orderInfo.getPaidAmount(), orderInfo.getStatus(), orderInfo.getCreatedAt());
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Long id;
        private Long userId;
        private BigDecimal totalPrice;
        private OrderStatus orderStatus;
        private ZonedDateTime createdAt;
        private List<OrderItemResponse> orderItemInfos;

        public static Detail of(OrderDetailInfo orderDetailInfo) {
            List<OrderItemResponse> orderItemResponses = orderDetailInfo.getOrderItemInfos().stream()
                    .map(OrderItemResponse::of)
                    .toList();
            return new Detail(
                    orderDetailInfo.getId(),
                    orderDetailInfo.getUserId(),
                    orderDetailInfo.getTotalPrice(),
                    orderDetailInfo.getOrderStatus(),
                    orderDetailInfo.getCreatedAt(),
                    orderItemResponses
            );
        }
    }

}
