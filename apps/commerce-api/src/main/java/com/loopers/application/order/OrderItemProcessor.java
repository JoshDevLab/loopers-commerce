package com.loopers.application.order;

import com.loopers.domain.inventory.Inventory;
import com.loopers.domain.inventory.InventoryHistory;
import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderItemProcessor {

    private final ProductOptionService productOptionService;
    private final InventoryService inventoryService;

    @Transactional
    public Result process(List<OrderCommand.OrderItemCommand> commands) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InventoryHistory> histories = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderCommand.OrderItemCommand cmd : commands) {
            ProductOption option = productOptionService.getOnSalesProductOption(cmd.getProductOptionId());
            Inventory inventory = inventoryService.getEnoughQuantityInventory(option, cmd.getQuantity());

            inventoryService.decreaseQuantity(inventory, cmd.getQuantity());
            histories.add(InventoryHistory.createDecrease(inventory, cmd.getQuantity()));
            orderItems.add(OrderItem.create(option, cmd.getQuantity()));

            totalAmount = totalAmount.add(option.getPrice().multiply(BigDecimal.valueOf(cmd.getQuantity())));
        }

        return new Result(totalAmount, orderItems, histories);
    }

    public record Result(
            BigDecimal totalAmount,
            List<OrderItem> orderItems,
            List<InventoryHistory> inventoryHistories
    ) {}
}

