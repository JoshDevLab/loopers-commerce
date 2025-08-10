package com.loopers.domain.inventory;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inventory_history")
public class InventoryHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @Enumerated(EnumType.STRING)
    private InventoryHistoryType inventoryHistoryType;
    private int quantityChanged;
    private int quantityBefore;
    private int quantityAfter;
    private String reason;

    private InventoryHistory(Inventory inventory,
                            InventoryHistoryType inventoryHistoryType,
                            int changedQuantity,
                            int quantityBefore,
                            int quantityAfter,
                            String reason) {
        this.inventory = inventory;
        this.inventoryHistoryType = inventoryHistoryType;
        this.quantityChanged = changedQuantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
    }

    private InventoryHistory(Inventory inventory,
                             Order order,
                             InventoryHistoryType inventoryHistoryType,
                             int changedQuantity,
                             int quantityBefore,
                             int quantityAfter,
                             String reason) {
        this.inventory = inventory;
        this.order = order;
        this.inventoryHistoryType = inventoryHistoryType;
        this.quantityChanged = changedQuantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
    }

    public static InventoryHistory createDecrease(Inventory inventory, int changedQuantity) {
        return new InventoryHistory(
                inventory,
                InventoryHistoryType.DECREASE,
                changedQuantity,
                inventory.getQuantity() + changedQuantity,
                inventory.getQuantity(),
                "주문"
        );
    }

    public static InventoryHistory createCancel(Inventory inventory, Order order, int quantityChanged) {
        return new InventoryHistory(
                inventory,
                order,
                InventoryHistoryType.ADJUSTMENT,
                quantityChanged,
                inventory.getQuantity() - quantityChanged,
                inventory.getQuantity(),
                "결제취소로 인한 이력 생성"
        );
    }

    public InventoryHistory setOrder(Order order) {
        this.order = order;
        return this;
    }
}
