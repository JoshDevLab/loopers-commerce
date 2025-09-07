package com.loopers.domain.inventory;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.ProductOption;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    private int quantity;

    public Inventory(ProductOption productOption, int quantity) {
        this.productOption = productOption;
        this.quantity = quantity;
    }

    public static Inventory create(ProductOption productOption, int quantity) {
        return new Inventory(productOption, quantity);
    }

    public void hasEnoughQuantity(int quantity) {
        if (this.quantity < quantity) {
            throw new CoreException(ErrorType.INSUFFICIENT_STOCK, "상품 재고가 없습니다.");
        }
    }

    public void decreaseQuantity(int quantity) {
        if (this.quantity - quantity < 0) {
            throw new CoreException(ErrorType.INSUFFICIENT_STOCK, "상품 재고가 없습니다.");
        }
        this.quantity -= quantity;
    }

    public void recovery(int quantityChanged) {
        this.quantity = quantityChanged;
    }

    public boolean isStockAdjusted() {
        return this.quantity == 0;
    }
}
