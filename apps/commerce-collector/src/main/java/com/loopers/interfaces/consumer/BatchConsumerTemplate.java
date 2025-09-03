package com.loopers.interfaces.consumer;

import com.loopers.application.IdempotentEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class BatchConsumerTemplate<E> {

    private final IdempotentEventProcessor idempotent;
    private final DltPublisher dltPublisher;

    /** DTO 유효성 검사 */
    protected abstract boolean isValid(E dto);

    /** 멱등성 키(보통 eventId) */
    protected abstract String eventId(E dto);

    /** 이벤트 타입(로그/모니터링용) */
    protected abstract String eventType(E dto);

    /** aggregateId가 있으면 제공 (없으면 null 반환해도 OK) */
    protected abstract String aggregateId(E dto);

    /** 실제 도메인 처리 */
    protected abstract void process(E dto);

    protected void sendToDlt(String srcTopic, String key, E dto, String reason, int partition, long offset) {
        dltPublisher.publish(srcTopic, key, dto, defaultMeta(reason), partition, offset);
    }

    protected boolean processIdempotently(String eventId, String eventType, String aggregateId, Runnable work) {
        return idempotent.processExactlyOnce(eventId, eventType, aggregateId, work);
    }

    /** 공통 배치 처리 엔진 */
    public final void handleBatch(
            String topic,
            List<E> events,
            List<String> keys,
            List<Integer> partitions,
            List<Long> offsets,
            Acknowledgment ack
    ) {
        if (events == null || events.isEmpty()) {
            ack.acknowledge();
            return;
        }
        if (keys == null || partitions == null || offsets == null
                || events.size() != keys.size()
                || events.size() != partitions.size()
                || events.size() != offsets.size()) {
            log.error("Payload/headers size mismatch: e={}, k={}, p={}, o={}",
                    events.size(),
                    keys == null ? -1 : keys.size(),
                    partitions == null ? -1 : partitions.size(),
                    offsets == null ? -1 : offsets.size());
            ack.acknowledge();
            return;
        }
        int processed = 0, invalid = 0, failed = 0, duplicate = 0;

        for (int i = 0; i < events.size(); i++) {
            E dto = events.get(i);
            String key = keys.get(i);
            Integer partition = partitions.get(i);
            Long offset = offsets.get(i);

            try {
                if (!isValid(dto)) {
                    sendToDlt(topic, key, dto, "invalid-payload", partition, offset);
                    invalid++;
                    continue;
                }

                boolean executed = processIdempotently(
                        eventId(dto),
                        eventType(dto),
                        aggregateId(dto),
                        () -> process(dto)
                );

                if (executed) processed++;
                else duplicate++;

            } catch (Exception e) {
                failed++;
                log.error("consume failed - topic={}, partition={}, offset={}, key={}, eventId={}",
                        topic, partition, offset, key, eventIdSafely(dto), e);
                try {
                    sendToDlt(topic, key, dto, e.getClass().getSimpleName(), partition, offset);
                } catch (Exception dltEx) {
                    log.error("DLT publish failed - topic={}, key={}, eventId={}",
                            topic, key, eventIdSafely(dto), dltEx);
                }
            }
        }

        ack.acknowledge();
        log.info("batch done: total={}, processed={}, invalid={}, failed={}",
                events.size(), processed, invalid, failed);
    }

    private String eventIdSafely(E dto) {
        try { return eventId(dto); } catch (Exception ignored) { return "unknown"; }
    }

    protected Map<String, String> defaultMeta(String reason) {
        Map<String, String> m = new HashMap<>();
        m.put("x-exception-reason", reason);
        return m;
    }
}
