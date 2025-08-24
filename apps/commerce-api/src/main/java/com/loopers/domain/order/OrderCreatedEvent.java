package com.loopers.domain.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        List<OrderCommand.OrderItemCommand> orderItemCommands,
        BigDecimal usedPoint,
        Long userCouponId,
        BigDecimal paidAmount,
        Long userPk,
        BigDecimal discountAmount) {
}
