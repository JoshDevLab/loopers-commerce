package com.loopers.interfaces.consumer.stockadjusted;

import com.loopers.application.IdempotentEventProcessor;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.stockadjusted.StockAdjustedService;
import com.loopers.interfaces.consumer.BatchConsumerTemplate;
import com.loopers.interfaces.consumer.DltPublisher;
import com.loopers.interfaces.consumer.stockadjusted.dto.StockAdjustedEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ProductStockAdjustedEventConsumer extends BatchConsumerTemplate<StockAdjustedEventDto> {
    private final StockAdjustedService stockAdjustedService;

    public ProductStockAdjustedEventConsumer(IdempotentEventProcessor idempotent, DltPublisher dltPublisher, StockAdjustedService stockAdjustedService) {
        super(idempotent, dltPublisher);
        this.stockAdjustedService = stockAdjustedService;
    }

    @Override
    protected boolean isValid(StockAdjustedEventDto dto) {
        return dto.isValid();
    }

    @Override
    protected String eventId(StockAdjustedEventDto dto) {
        return dto.getEventId();
    }

    @Override
    protected String eventType(StockAdjustedEventDto dto) {
        return dto.getEventType();
    }

    @Override
    protected String aggregateId(StockAdjustedEventDto dto) {
        return dto.getAggregateId();
    }

    @Override
    protected void process(StockAdjustedEventDto dto) {
        stockAdjustedService.StockAdjusted(dto.toCommand());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.product-view-events:product-view-events}",
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "${app.kafka.consumer-groups.product-view-collector:product-view-collector}"
    )
    public void onMessage(
            @Payload List<StockAdjustedEventDto> events,
            @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets,
            @Header(KafkaHeaders.RECEIVED_TOPIC) List<String> topics,
            Acknowledgment ack
    ) {
        String topic = topics.isEmpty() ? "unknown" : topics.getFirst();
        handleBatch(topic, events, keys, partitions, offsets, ack);
    }
}
