package com.loopers.infrastructure.inventory;

import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.product.ProductOption;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryJapRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductOption(ProductOption productOption);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.productOption = :productOption")
    Optional<Inventory> findByProductOptionWithLock(ProductOption productOption);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.productOption.id = :productOptionId")
    Optional<Inventory> findByProductOptionIdWithLock(Long productOptionId);
}
