package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.infrastructure.support.RedisDistributedLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
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
            // Kafka 발행
            kafkaTemplate.send(
                    event.getTopicName(),
                    event.getAggregateId(),  // 파티션 키
                    event
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
}
