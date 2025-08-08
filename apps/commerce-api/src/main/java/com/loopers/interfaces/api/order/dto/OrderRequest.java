package com.loopers.interfaces.api.order.dto;

import com.loopers.domain.order.Address;
import com.loopers.domain.order.OrderCommand;

import java.util.Comparator;
import java.util.List;

public record OrderRequest(
        List<OrderItemRequest> orderItemRequests,
        AddressRequest addressRequest,
        Long userCouponId
) {
    public OrderCommand.Register toRegisterCommand() {
        List<OrderCommand.OrderItemCommand> orderItemCommands = orderItemRequests.stream()
                .sorted(Comparator.comparing(OrderItemRequest::productOptionId))
                .map(OrderItemRequest::toCommand)
                .toList();
        return new OrderCommand.Register(orderItemCommands, addressRequest.to(), userCouponId);
    }

    public record OrderItemRequest(
            Long productOptionId,
            int quantity
    ) {
        public OrderCommand.OrderItemCommand toCommand() {
            return new OrderCommand.OrderItemCommand(this.productOptionId, this.quantity);
        }
    }

    public record AddressRequest(
            String zipCode,
            String roadAddress,
            String detailAddress,
            String receiverName,
            String receiverPhone
    ) {
        public Address to() {
            return new Address(this.zipCode, this.roadAddress, this.detailAddress, this.receiverName, this.receiverPhone);
        }
    }

    public record OrderSearchConditionRequest(
        String sort
    ) {

    }
}
