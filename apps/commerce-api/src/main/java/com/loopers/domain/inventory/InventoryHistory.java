package com.loopers.domain.inventory;

import com.loopers.domain.BaseEntity;
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

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private InventoryHistoryType inventoryHistoryType;
    private int quantityChanged;
    private int quantityBefore;
    private int quantityAfter;
    private String reason;

    private InventoryHistory(Inventory inventory,
                             Long orderId,
                             InventoryHistoryType inventoryHistoryType,
                             int changedQuantity,
                             int quantityBefore,
                             int quantityAfter,
                             String reason) {
        this.inventory = inventory;
        this.orderId = orderId;
        this.inventoryHistoryType = inventoryHistoryType;
        this.quantityChanged = changedQuantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
    }

    public static InventoryHistory createDecrease(Inventory inventory, int changedQuantity, Long orderId, String reason) {
        InventoryHistory inventoryHistory = new InventoryHistory();
        inventoryHistory.inventory = inventory;
        inventoryHistory.orderId = orderId;
        inventoryHistory.quantityChanged = changedQuantity;
        inventoryHistory.quantityBefore = inventory.getQuantity() + changedQuantity;
        inventoryHistory.quantityAfter = inventory.getQuantity();
        inventoryHistory.reason = reason;
        return inventoryHistory;
    }

    public static InventoryHistory createCancel(Inventory inventory, Long orderId, int quantityChanged) {
        return new InventoryHistory(
                inventory,
                orderId,
                InventoryHistoryType.ADJUSTMENT,
                quantityChanged,
                inventory.getQuantity() - quantityChanged,
                inventory.getQuantity(),
                "결제취소로 인한 이력 생성"
        );
    }

}
