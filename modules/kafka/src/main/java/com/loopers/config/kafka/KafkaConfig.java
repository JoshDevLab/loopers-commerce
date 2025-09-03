package com.loopers.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String BATCH_LISTENER = "BATCH_LISTENER_DEFAULT";

    private static final int MAX_POLL_RECORDS = 3000;
    private static final int FETCH_MIN_BYTES = (1024 * 1024);
    private static final int FETCH_MAX_WAIT_MS = 5_000;

    /* =========================
     * Producer (멱등성/안정성)
     * ========================= */
    @Bean
    public ProducerFactory<Object, Object> producerFactory(KafkaProperties props) {
        Map<String, Object> cfg = new HashMap<>(props.buildProducerProperties());
        cfg.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        cfg.put(ProducerConfig.ACKS_CONFIG, "all");
        cfg.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // 최신 브로커 OK
        cfg.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        cfg.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 300_000); // 5분
        return new DefaultKafkaProducerFactory<>(cfg);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

    /* =========================
     * DLT 전용 Producer (원본 보존)
     *  - key:String, value:byte[] 그대로
     * ========================= */
    @Bean
    public ProducerFactory<String, byte[]> dltProducerFactory(KafkaProperties props) {
        Map<String, Object> cfg = new HashMap<>(props.buildProducerProperties());
        cfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(cfg);
    }

    @Bean
    public KafkaTemplate<String, byte[]> dltKafkaTemplate(ProducerFactory<String, byte[]> pf) {
        return new KafkaTemplate<>(pf);
    }

    /* =========================
     * Consumer
     * ========================= */
    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory(KafkaProperties props) {
        Map<String, Object> cfg = new HashMap<>(props.buildConsumerProperties());
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        // max.poll.records/fetch 관련은 컨테이너 팩토리에서 override 해도 되고 여기서 줘도 됨
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ByteArrayJsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new ByteArrayJsonMessageConverter(objectMapper);
    }

    /* =========================
     * 에러 핸들러 (재시도 → 실패 시 DLT)
     *  - 메서드 바깥(역직렬화 등)에서 난 예외도 처리
     * ========================= */
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, byte[]> dltTemplate) {
        // 기본: <원본토픽>.DLT 로 보냄. 필요 시 (rec, ex) -> new TopicPartition(...) 커스텀 가능
        return new DeadLetterPublishingRecoverer(dltTemplate);
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(500);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5_000);

        DefaultErrorHandler eh = new DefaultErrorHandler(recoverer, backOff);

        // 필요 시 재시도/비재시도 예외 타이핑
        // eh.addNotRetryableExceptions(IllegalArgumentException.class);

        return eh;
    }

    /* =========================
     * Listener Container (배치+수동커밋)
     * ========================= */
    @Bean(name = BATCH_LISTENER)
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> batchListenerFactory(
            ConsumerFactory<String, byte[]> consumerFactory,
            ByteArrayJsonMessageConverter converter,
            CommonErrorHandler errorHandler
    ) {
        // consumerFactory의 설정을 복사해서 배치/세부 튜닝
        Map<String, Object> cfg = new HashMap<>(consumerFactory.getConfigurationProperties());
        cfg.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        cfg.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, FETCH_MIN_BYTES);
        cfg.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, FETCH_MAX_WAIT_MS);
        // MAX_POLL_INTERVAL_MS 는 yml에서 10분 권장 (여기서도 줄 수 있음)

        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(cfg));

        factory.setBatchListener(true);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(converter));

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);

        factory.setCommonErrorHandler(errorHandler);

        // 파티션 수와 트래픽에 맞춰 조정(운영에서 외부 설정으로 꺼내두면 더 좋음)
        factory.setConcurrency(3);

        return factory;
    }
}
