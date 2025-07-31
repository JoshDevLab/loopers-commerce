package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailInfo {
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private ZonedDateTime createdAt;
    private List<OrderItemInfo> orderItemInfos;

    public static OrderDetailInfo from(Order order, List<OrderItem> orderItems) {
        List<OrderItemInfo> orderItemInfos = orderItems.stream().map(OrderItemInfo::from).toList();
        return new OrderDetailInfo(
                order.getId(),
                order.getUser().getId(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getCreatedAt(),
                orderItemInfos
        );
    }
}
