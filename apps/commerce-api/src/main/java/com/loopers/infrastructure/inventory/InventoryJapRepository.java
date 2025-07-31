package com.loopers.infrastructure.inventory;

import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryJapRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductOption(ProductOption productOption);
}
