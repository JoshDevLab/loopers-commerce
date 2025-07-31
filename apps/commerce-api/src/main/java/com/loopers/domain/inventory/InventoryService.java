package com.loopers.domain.inventory;

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

    public Inventory hasEnoughQuantity(ProductOption productOption, int quantity) {
        Inventory inventory = inventoryRepository.findByProductOption(productOption)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_INVENTORY_NOT_FOUND, "상품 재고를 찾을 수 없습니다."));
        inventory.hasEnoughQuantity(quantity);
        return inventory;
    }

    @Transactional
    public Inventory decreaseQuantity(Inventory inventory, int quantity) {
        inventory.decreaseQuantity(quantity);
        return inventory;
    }

}
