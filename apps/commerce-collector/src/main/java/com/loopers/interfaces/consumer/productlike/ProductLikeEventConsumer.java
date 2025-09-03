package com.loopers.interfaces.consumer.productlike;

import com.loopers.application.IdempotentEventProcessor;
import com.loopers.application.productlike.ProductLikeEventProcessor;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.interfaces.consumer.BatchConsumerTemplate;
import com.loopers.interfaces.consumer.DltPublisher;
import com.loopers.interfaces.consumer.productlike.dto.ProductLikeEventDto;
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
public class ProductLikeEventConsumer extends BatchConsumerTemplate<ProductLikeEventDto> {
    private final ProductLikeEventProcessor processor;

    public ProductLikeEventConsumer(IdempotentEventProcessor idempotent, DltPublisher dltPublisher, ProductLikeEventProcessor processor) {
        super(idempotent, dltPublisher);
        this.processor = processor;
    }

    @Override protected boolean isValid(ProductLikeEventDto dto) { return dto != null && dto.isValid(); }
    @Override protected String  eventId(ProductLikeEventDto dto) { return dto.getEventId(); }
    @Override protected String  eventType(ProductLikeEventDto dto){ return dto.getEventType(); }
    @Override protected String aggregateId(ProductLikeEventDto dto) {return dto.getAggregateId();}

    @Override
    protected void process(ProductLikeEventDto dto) {
        processor.processEvent(dto.toCommand());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.product-like-events:product-like-events}",
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "${app.kafka.consumer-groups.product-like-collector:product-like-collector}"
    )
    public void onMessage(
            @Payload List<ProductLikeEventDto> events,
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
