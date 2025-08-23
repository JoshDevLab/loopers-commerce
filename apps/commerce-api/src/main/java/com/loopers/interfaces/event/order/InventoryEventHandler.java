package com.loopers.interfaces.event.order;

import com.loopers.domain.inventory.InventoryService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.text.MessageFormat;

@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryEventHandler {
    private final InventoryService inventoryService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling order created event: {}", event);

        for (OrderCommand.OrderItemCommand command : event.orderItemCommands()) {
            inventoryService.decreaseQuantity(command.getProductOptionId(),
                    event.orderId(),
                    command.getQuantity(),
                    MessageFormat.format("Order Id : {0} 주문", event.orderId()));
        }
    }
}
