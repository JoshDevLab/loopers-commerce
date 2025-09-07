package com.loopers.interfaces.consumer.order;

import com.loopers.application.IdempotentEventProcessor;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.productmetrics.ProductMetricsService;
import com.loopers.interfaces.consumer.BatchConsumerTemplate;
import com.loopers.interfaces.consumer.DltPublisher;
import com.loopers.interfaces.consumer.order.dto.ProductOrderEventDto;
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
public class ProductOrderEventConsumer extends BatchConsumerTemplate<ProductOrderEventDto> {
    private final ProductMetricsService productMetricsService;

    public ProductOrderEventConsumer(IdempotentEventProcessor idempotent, DltPublisher dltPublisher, ProductMetricsService productMetricsService) {
        super(idempotent, dltPublisher);
        this.productMetricsService = productMetricsService;
    }

    @Override
    protected boolean isValid(ProductOrderEventDto dto) {
        return dto.isValid();
    }

    @Override
    protected String eventId(ProductOrderEventDto dto) {
        return dto.getEventId();
    }

    @Override
    protected String eventType(ProductOrderEventDto dto) {
        return dto.getEventType();
    }

    @Override
    protected String aggregateId(ProductOrderEventDto dto) {
        return dto.getAggregateId();
    }

    @Override
    protected void process(ProductOrderEventDto dto) {
        productMetricsService.metricProductOrder(dto.toCommand());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.product-order-events:product-order-events}",
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "${app.kafka.consumer-groups.product-order-collector:product-order-collector}"
    )
    public void onMessage(
            @Payload List<ProductOrderEventDto> events,
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
