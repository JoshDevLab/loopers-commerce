package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "product_options")
@Entity
public class ProductOption extends BaseEntity {

    private String size;
    private String color;
    private ProductStatus productOptionStatus;
    private BigDecimal price;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private ProductOption(String size, String color, ProductStatus productOptionStatus, BigDecimal price, Product product) {
        this.size = size;
        this.color = color;
        this.productOptionStatus = productOptionStatus;
        this.price = price;
        this.product = product;
    }

    public static ProductOption create(String size, String color, ProductStatus productOptionStatus, BigDecimal price, Product product) {
        return new ProductOption(size, color, productOptionStatus, price, product);
    }
}
