package com.loopers.interfaces.consumer.ranking;

import com.loopers.application.IdempotentEventProcessor;
import com.loopers.application.ranking.WeightConfigEventProcessor;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.interfaces.consumer.BatchConsumerTemplate;
import com.loopers.interfaces.consumer.DltPublisher;
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
public class WeightConfigEventConsumer extends BatchConsumerTemplate<WeightConfigEventDto> {

    private final WeightConfigEventProcessor processor;

    public WeightConfigEventConsumer(IdempotentEventProcessor idempotent,
                                     DltPublisher dltPublisher,
                                     WeightConfigEventProcessor processor) {
        super(idempotent, dltPublisher);
        this.processor = processor;
    }

    @Override
    protected boolean isValid(WeightConfigEventDto dto) {
        return dto != null && dto.isValid();
    }

    @Override
    protected String eventId(WeightConfigEventDto dto) {
        return dto.getEventId();
    }

    @Override
    protected String eventType(WeightConfigEventDto dto) {
        return dto.getEventType();
    }

    @Override
    protected String aggregateId(WeightConfigEventDto dto) {
        return dto.getAggregateId();
    }

    @Override
    protected void process(WeightConfigEventDto dto) {
        processor.processEvent(dto.toCommand());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.weight-config-events:weight-config-events}",
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "${app.kafka.consumer-groups.weight-config-collector:weight-config-collector}"
    )
    public void onMessage(
            @Payload List<WeightConfigEventDto> events,
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
