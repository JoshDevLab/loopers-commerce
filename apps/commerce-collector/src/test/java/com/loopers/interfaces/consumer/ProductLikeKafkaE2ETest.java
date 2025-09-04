package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.eventhandled.EventHandled;
import com.loopers.domain.productmetrics.ProductMetrics;
import com.loopers.infrastructure.eventhandled.EventHandledJpaRepository;
import com.loopers.infrastructure.productmetrics.ProductMetricsJpaRepository;
import com.loopers.support.KafkaTestInitializer;
import com.loopers.support.TxReadHelper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = KafkaTestInitializer.class)
class ProductLikeKafkaE2ETest {

    @Autowired TxReadHelper txReadHelper;
    @Autowired ObjectMapper objectMapper;
    @Autowired KafkaTemplate<String, byte[]> kafkaTemplate; // 네 모듈의 ProducerFactory 설정 재사용
    @Autowired ProductMetricsJpaRepository productMetricsRepo;
    @Autowired EventHandledJpaRepository eventHandledRepo;

    private final String topic = "product-like-events";
    private final String dltTopic = topic + ".DLT";

    @Test
    @DisplayName("정상 이벤트 → 리스너 처리 → DB 반영")
    void successFlow() throws Exception {
        String eventId = UUID.randomUUID().toString();

        byte[] payload = objectMapper.writeValueAsBytes(Map.of(
                "eventId", eventId,
                "eventType", "PRODUCT_LIKED",
                "productId", 123L,
                "aggregateId", "product-123",
                "timestamp", "2025-09-03T12:34:56.789+09:00"
        ));

        kafkaTemplate.send(topic, "key-1", payload).get(5, TimeUnit.SECONDS);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(productMetricsRepo.count()).isGreaterThan(0);
            assertThat(eventHandledRepo.findByEventId(eventId)).isPresent();
        });
    }

    @Test
    @DisplayName("유효성 오류 → DLT 격리")
    void invalidGoesToDLT(@Autowired org.springframework.kafka.core.ConsumerFactory<String, byte[]> cf)
            throws Exception {

        byte[] invalid = objectMapper.writeValueAsBytes(Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "PRODUCT_LIKED"
                // productId 없음 -> isValid=false -> DLT
        ));

        kafkaTemplate.send(topic, "bad", invalid).get(5, TimeUnit.SECONDS);

        try (var consumer = cf.createConsumer("dlt-checker-" + UUID.randomUUID(), null)) {
            consumer.subscribe(java.util.List.of(dltTopic));
            Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                var records = consumer.poll(java.time.Duration.ofMillis(500));
                assertThat(records.count()).isGreaterThan(0);
            });
        }
    }

    @Test
    @DisplayName("멱등성: 동일 eventId 두 번 → 한 번만 처리")
    void idempotency() throws Exception {
        String eventId = UUID.randomUUID().toString();

        byte[] payload = objectMapper.writeValueAsBytes(Map.of(
                "eventId", eventId,
                "eventType", "PRODUCT_LIKED",
                "productId", 345L,
                "aggregateId", "product-345",
                "timestamp", "2025-09-03T12:34:56.789+09:00"
        ));

        kafkaTemplate.send(topic, "dup", payload).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send(topic, "dup", payload).get(5, TimeUnit.SECONDS);

        // 1) 처리 시작/완료가 적어도 한 번 기록됨
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(eventHandledRepo.existsByEventId(eventId)).isTrue();
        });

        // 2) 최종적으로 행이 '정확히 1개'만 존재
        assertThat(eventHandledRepo.countByEventId(eventId)).isEqualTo(1L);
    }
}
