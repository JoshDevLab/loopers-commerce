package com.loopers.interfaces.consumer.productview;

import com.loopers.application.IdempotentEventProcessor;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.productmetrics.ProductMetricsService;
import com.loopers.interfaces.consumer.BatchConsumerTemplate;
import com.loopers.interfaces.consumer.DltPublisher;
import com.loopers.interfaces.consumer.productview.dto.ProductViewEventDto;
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
public class ProductViewEventConsumer extends BatchConsumerTemplate<ProductViewEventDto> {
    private final ProductMetricsService productMetricsService;

    public ProductViewEventConsumer(IdempotentEventProcessor idempotent, DltPublisher dltPublisher, ProductMetricsService productMetricsService) {
        super(idempotent, dltPublisher);
        this.productMetricsService = productMetricsService;
    }

    @Override
    protected boolean isValid(ProductViewEventDto dto) {
        return dto.isValid();
    }

    @Override
    protected String eventId(ProductViewEventDto dto) {
        return dto.getEventId();
    }

    @Override
    protected String eventType(ProductViewEventDto dto) {
        return dto.getEventType();
    }

    @Override
    protected String aggregateId(ProductViewEventDto dto) {
        return dto.getAggregateId();
    }

    @Override
    protected void process(ProductViewEventDto dto) {
        productMetricsService.metricProductView(dto.toCommand());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.product-view-events:product-view-events}",
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "${app.kafka.consumer-groups.product-view-collector:product-view-collector}"
    )
    public void onMessage(
            @Payload List<ProductViewEventDto> events,
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
