package com.loopers.infrastructure.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.infrastructure.support.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventScheduler {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RedisDistributedLock distributedLock;

    @Value("${scheduling.tasks.outbox-publishing.batch-size:100}")
    private int batchSize;

    private static final String OUTBOX_LOCK_KEY = "outbox:publish:lock";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(5);

    @Scheduled(fixedDelayString = "#{${scheduling.tasks.outbox-publishing.interval-seconds:5} * 1000}")
    @ConditionalOnProperty(name = "scheduling.tasks.outbox-publishing.enabled", havingValue = "true", matchIfMissing = true)
    public void publishPendingEvents() {
        distributedLock.executeWithLock(OUTBOX_LOCK_KEY, LOCK_TIMEOUT, this::doPublishEvents);
    }

    private void doPublishEvents() {
        List<OutboxEvent> events = outboxEventRepository.findUnpublishedEvents(batchSize);

        if (events.isEmpty()) {
            return;
        }

        log.info("아웃박스 이벤트 발행 시작: {} 건", events.size());

        int successCount = 0;
        for (OutboxEvent event : events) {
            try {
                publishEvent(event);
                successCount++;
            } catch (Exception e) {
                log.error("이벤트 발행 실패: eventId={}", event.getEventId(), e);
            }
        }

        log.info("아웃박스 이벤트 발행 완료: {} / {} 건", successCount, events.size());
    }

    private void publishEvent(OutboxEvent event) {
        try {
            String enrichedPayload = addEventMetadataToPayload(event.getPayload(),
                    event.getEventId(),
                    event.getEventType().toString(),
                    event.getAggregateId());

            // Kafka 발행
            kafkaTemplate.send(
                    event.getTopicName(),
                    event.getAggregateId(),  // 파티션 키
                    enrichedPayload
            );

            // 발행 완료 처리
            outboxEventRepository.markAsPublished(event.getEventId());

            log.debug("이벤트 발행 성공: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("이벤트 발행 실패: eventId={}", event.getEventId(), e);
            throw e;
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    @ConditionalOnProperty(name = "scheduling.tasks.outbox-cleanup.enabled", havingValue = "true", matchIfMissing = true)
    @Profile({"dev", "qa", "prd"})
    @Transactional
    public void cleanupOldEvents() {
        ZonedDateTime threshold = ZonedDateTime.now().minusDays(7);
        outboxEventRepository.deleteOldPublishedEvents(threshold);
        log.info("오래된 아웃박스 이벤트 정리 완료");
    }

    private String addEventMetadataToPayload(String originalPayload, String eventId,
                                             String eventType, String aggregateId) {
        try {
            JsonNode jsonNode = objectMapper.readTree(originalPayload);

            if (jsonNode instanceof ObjectNode objectNode) {
                // 필수 메타데이터 추가
                objectNode.put("eventId", eventId);
                objectNode.put("eventType", eventType);
                objectNode.put("aggregateId", aggregateId);

                // 타임스탬프 추가 (Consumer에서 발생 시점 확인용)
                objectNode.put("timestamp", ZonedDateTime.now().toString());

                return objectMapper.writeValueAsString(objectNode);
            }

            return originalPayload;
        } catch (Exception e) {
            log.error("Payload에 메타데이터 추가 실패: {}", originalPayload, e);
            return originalPayload; // 실패시 원본 반환
        }
    }
}
