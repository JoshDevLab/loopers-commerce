package com.loopers.domain.inventory;

import com.loopers.domain.outbox.OutboxEventPublisher;
import com.loopers.domain.product.ProductOption;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void hasEnoughQuantityInventory(ProductOption productOption, int quantity) {
        Inventory inventory = inventoryRepository.findByProductOption(productOption)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_INVENTORY_NOT_FOUND, "상품 재고를 찾을 수 없습니다."));
        inventory.hasEnoughQuantity(quantity);
    }

    @Transactional
    public void decreaseQuantity(Long productOptionId,  Long orderId, int quantity, String reason) {
        Inventory inventory = inventoryRepository.findByProductOptionIdWithLock(productOptionId)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_INVENTORY_NOT_FOUND, "상품 재고를 찾을 수 없습니다."));
        inventory.hasEnoughQuantity(quantity);
        inventory.decreaseQuantity(quantity);
        if (inventory.isStockAdjusted()) {
            outboxEventPublisher.publish(new StockAdjustedEvent(productOptionId));
        }
        inventoryHistoryRepository.save(InventoryHistory.createDecrease(inventory, quantity, orderId, reason));
    }

    @Transactional
    public void recovery(Long orderId) {
        InventoryHistory inventoryHistory = inventoryHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.INVENTORY_HISTORY_NOT_FOUND, "재고 이력을 찾을 수 없습니다."));

        Inventory inventory = inventoryRepository.findById(inventoryHistory.getInventory().getId())
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_INVENTORY_NOT_FOUND, "상품 재고를 찾을 수 없습니다."));

        inventory.recovery(inventoryHistory.getQuantityChanged());

        InventoryHistory history = InventoryHistory.createCancel(inventoryHistory.getInventory(),
                orderId,
                inventoryHistory.getQuantityChanged());
        inventoryHistoryRepository.save(history);
    }
}
