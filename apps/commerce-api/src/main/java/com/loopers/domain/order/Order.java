package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Embedded
    private Address shippingAddress;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(User user, Address shippingAddress, BigDecimal paidAmount, OrderStatus orderStatus) {
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.totalAmount = paidAmount;
        this.orderStatus = orderStatus;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public static Order create(User user, OrderCommand.Register orderCommand, BigDecimal paidAmount) {
        return new Order(user, orderCommand.getAddress(), paidAmount,  OrderStatus.PENDING);
    }
}
