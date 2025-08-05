package com.loopers.infrastructure.inventory;

import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryRepository;
import com.loopers.domain.product.ProductOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryJapRepository inventoryJapRepository;

    @Override
    public Optional<Inventory> findByProductOption(ProductOption productOption) {
        return inventoryJapRepository.findByProductOption(productOption);
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryJapRepository.save(inventory);
    }

    @Override
    public Optional<Inventory> findByProductOptionWithLock(ProductOption productOption) {
        return inventoryJapRepository.findByProductOptionWithLock(productOption);
    }

}
