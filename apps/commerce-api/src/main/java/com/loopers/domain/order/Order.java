package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    private BigDecimal paidAmount;

    private BigDecimal totalAmount;
    
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(User user, Address shippingAddress, BigDecimal paidAmount, BigDecimal discountAmount, OrderStatus orderStatus) {
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.totalAmount = paidAmount;
        this.discountAmount = discountAmount;
        this.orderStatus = orderStatus;
    }

    public Order(User user, Address address, BigDecimal paidAmount, BigDecimal totalAmount, BigDecimal discountAmount, OrderStatus orderStatus) {
        this.user = user;
        this.shippingAddress = address;
        this.paidAmount = paidAmount;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.orderStatus = orderStatus;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public static Order create(User user, OrderCommand.Register orderCommand, BigDecimal totalAmount, BigDecimal discountAmount) {
        BigDecimal paidAmount = totalAmount.subtract(discountAmount);
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.INVALID_PAID_AMOUNT, "총 결제 금액은 0원 이상이어야 합니다.");
        }
        return new Order(user, orderCommand.getAddress(), paidAmount, totalAmount, discountAmount, OrderStatus.PENDING);
    }
}
