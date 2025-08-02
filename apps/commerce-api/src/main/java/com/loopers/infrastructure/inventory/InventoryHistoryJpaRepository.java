package com.loopers.infrastructure.inventory;

import com.loopers.domain.inventory.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryJpaRepository extends JpaRepository<InventoryHistory, Long> {
}
