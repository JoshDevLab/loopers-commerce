package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.user.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private Long id;
    private BigDecimal paidAmount;

    public static OrderInfo from(Order order) {
        return new OrderInfo(order.getId(), order.getTotalAmount());
    }
}
