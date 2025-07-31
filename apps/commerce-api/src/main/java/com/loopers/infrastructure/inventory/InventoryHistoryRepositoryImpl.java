package com.loopers.infrastructure.inventory;

import com.loopers.domain.inventory.InventoryHistory;
import com.loopers.domain.inventory.InventoryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class InventoryHistoryRepositoryImpl implements InventoryHistoryRepository {
    private final InventoryHistoryJpaRepository inventoryHistoryJpaRepository;

    @Override
    public InventoryHistory save(InventoryHistory inventoryHistory) {
        return inventoryHistoryJpaRepository.save(inventoryHistory);
    }
}
