package com.loopers.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class OrderCommand {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Register {
        private List<OrderItemCommand> orderItemCommands;
        private Address address;
        private Long userCouponId;
        private BigDecimal usedPoint;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemCommand {
        private Long productOptionId;
        private int quantity;
    }

}
