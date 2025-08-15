package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "product_options")
@Entity
public class ProductOption extends BaseEntity {
    private String name;
    private String size;
    private String color;

    @Enumerated(EnumType.STRING)
    private ProductStatus productOptionStatus;
    private BigDecimal price;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private ProductOption(String name, String size, String color, ProductStatus productOptionStatus, BigDecimal price, Product product) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.productOptionStatus = productOptionStatus;
        this.price = price;
        this.product = product;
    }

    public static ProductOption create(String name, String size, String color, ProductStatus productOptionStatus, BigDecimal price, Product product) {
        return new ProductOption(name, size, color, productOptionStatus, price, product);
    }

    public void isOnSales() {
        if (!this.productOptionStatus.equals(ProductStatus.ON_SALE)) {
            throw new CoreException(ErrorType.PRODUCT_OPTION_NOT_ON_SALE, this.getId() + "는 판매중인 상품이 아닙니다.");
        }
    }
}
