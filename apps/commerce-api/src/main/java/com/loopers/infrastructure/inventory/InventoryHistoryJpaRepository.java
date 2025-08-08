package com.loopers.infrastructure.inventory;

import com.loopers.domain.inventory.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryHistoryJpaRepository extends JpaRepository<InventoryHistory, Long> {
    Optional<InventoryHistory> findByOrderId(Long orderId);
}
