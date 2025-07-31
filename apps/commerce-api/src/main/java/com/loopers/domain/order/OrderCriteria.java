package com.loopers.domain.order;

import com.loopers.interfaces.api.order.dto.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCriteria {
    private String sort;

    public static OrderCriteria toCriteria(OrderRequest.OrderSearchConditionRequest condition) {
        return new OrderCriteria(condition.sort());
    }

}
