package com.loopers.interfaces.consumer.auditlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.auditlog.AuditLogService;
import com.loopers.interfaces.consumer.DltPublisher;
import com.loopers.interfaces.consumer.auditlog.dto.UniversalEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventConsumer {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final DltPublisher dltPublisher;

    @KafkaListener(
            topics = {"${app.kafka.topics.product-like-events:product-like-events}",
                    "${app.kafka.topics.product-order-events:product-order-events}",
                    "${app.kafka.topics.product-view-events:product-view-events}"},
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "${app.kafka.consumer-groups.audit-collector:audit-collector}"
    )
    public void onAuditBatch(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        if (records == null || records.isEmpty()) {
            ack.acknowledge();
            return;
        }

        int processed = 0, skipped = 0, failed = 0;

        for (ConsumerRecord<String, byte[]> rec : records) {
            final String key = rec.key();
            final int partition = rec.partition();
            final long offset = rec.offset();

            try {
                // 1) 원문 유지 + 역직렬화
                String raw = rec.value() == null ? "" : new String(rec.value(), StandardCharsets.UTF_8);
                UniversalEventDto dto = objectMapper.readValue(raw, UniversalEventDto.class);
                dto.setRawPayload(raw);
                dto.setTopicName(rec.topic());

                // 2) 유효성 검증
                if (!dto.isValid()) {
                    sendDlt(rec.topic(), key, raw, "invalid-payload", partition, offset);
                    skipped++;
                    continue;
                }

                // 3) 도메인 처리
                auditLogService.logEvent(dto.toCommand());
                processed++;

            } catch (Exception e) {
                failed++;
                log.error("audit consume failed - topic={}, partition={}, offset={}, key={}",
                        rec.topic(), partition, offset, key, e);
                try {
                    String raw = rec.value() == null ? "" : new String(rec.value(), StandardCharsets.UTF_8);
                    sendDlt(rec.topic(), key, raw, e.getClass().getSimpleName(), partition, offset);
                } catch (Exception dltEx) {
                    log.error("audit DLT publish failed - topic={}, key={}, partition={}, offset={}",
                            rec.topic(), key, partition, offset, dltEx);
                }
            }
        }

        ack.acknowledge();

        log.info("audit batch done: total={}, processed={}, skipped={}, failed={}",
                records.size(), processed, skipped, failed);
    }

    private void sendDlt(String srcTopic, String key, Object payload, String reason, int partition, long offset) {
        Map<String, String> meta = new HashMap<>();
        meta.put("x-exception-reason", reason);
        meta.put("x-src-partition", String.valueOf(partition));
        meta.put("x-src-offset", String.valueOf(offset));
        dltPublisher.publish(srcTopic, key, payload, meta, partition, offset);
    }
}
