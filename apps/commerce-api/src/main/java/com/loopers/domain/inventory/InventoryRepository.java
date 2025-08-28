package com.loopers.domain.inventory;

import com.loopers.domain.product.ProductOption;

import java.util.Optional;

public interface InventoryRepository {
    Optional<Inventory> findByProductOption(ProductOption productOption);

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(Long id);

    Optional<Inventory> findByProductOptionIdWithLock(Long productOptionId);
}
