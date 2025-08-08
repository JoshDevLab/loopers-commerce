package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private Long id;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private OrderStatus status;
    private ZonedDateTime createdAt;

    public static OrderInfo from(Order order) {
        return new OrderInfo(order.getId(), order.getTotalAmount(), order.getDiscountAmount(), order.getPaidAmount(), order.getOrderStatus(), order.getCreatedAt());
    }

    public static OrderInfo fromForSearch(Order order) {
        return new OrderInfo(order.getId(), order.getTotalAmount(), order.getDiscountAmount(), order.getPaidAmount(), order.getOrderStatus(), order.getCreatedAt());
    }

}
