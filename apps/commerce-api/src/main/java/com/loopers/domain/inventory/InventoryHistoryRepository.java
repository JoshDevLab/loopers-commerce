package com.loopers.domain.inventory;


import java.util.Optional;

public interface InventoryHistoryRepository {
    InventoryHistory save(InventoryHistory inventoryHistory);
    Optional<InventoryHistory> findByOrderId(Long orderId);
}
