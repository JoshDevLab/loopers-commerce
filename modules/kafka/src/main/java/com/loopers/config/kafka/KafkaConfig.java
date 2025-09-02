package com.loopers.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

    private static final int MAX_POLLING_SIZE = 3000;
    private static final int FETCH_MIN_BYTES = (1024 * 1024);
    private static final int FETCH_MAX_WAIT_MS = 5 * 1000;
    private static final int SESSION_TIMEOUT_MS = 60 * 1000;
    private static final int HEARTBEAT_INTERVAL_MS = 20 * 1000;
    private static final int MAX_POLL_INTERVAL_MS = 2 * 60 * 1000;

    @Bean
    public ProducerFactory<Object, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);

        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 300_000);   // 5분

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ByteArrayJsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new ByteArrayJsonMessageConverter(objectMapper);
    }

    /**
     * 기본 DeadLetterPublishingRecoverer:
     *  - 실패한 원본 topic에 ".DLT"를 붙인 토픽으로 전송 (예: order-events -> order-events.DLT)
     *  - 파티션 결정: 원본 레코드와 동일 키 해시 기반 (기본 전략)
     *  - 필요 시 (topic, ex) -> new TopicPartition("custom-dlt", somePartition) 로직 제공 가능
     */
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate);
    }

    /**
     * DefaultErrorHandler (Spring Kafka 3.x):
     *  - 재시도 후에도 실패하면 DLT로 publish
     *  - 역직렬화/리밸런스/치명적 예외 등을 포함해 세부 포함/제외 가능
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        // 지수 백오프 재시도: 최대 5회, 초기 500ms, 배수 2.0
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(500);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5_000);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 필요 시 무재시도(exclude) 예외 지정 예:
        // errorHandler.addNotRetryableExceptions(ValidationException.class);

        // 필요 시 재시도 대상(include) 예외 지정 예:
        // errorHandler.addRetryableExceptions(RetriableException.class);

        return errorHandler;
    }

    @Bean(name = BATCH_LISTENER)
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> defaultBatchListenerContainerFactory(
            ConsumerFactory<String, byte[]> consumerFactory,
            ByteArrayJsonMessageConverter converter,
            CommonErrorHandler kafkaErrorHandler
    ) {
        Map<String, Object> cfg = new HashMap<>(consumerFactory.getConfigurationProperties());
        cfg.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLLING_SIZE);
        cfg.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, FETCH_MIN_BYTES);
        cfg.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, FETCH_MAX_WAIT_MS);
        cfg.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);
        cfg.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS);
        cfg.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS);

        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(cfg));

        factory.setBatchListener(true);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(converter));

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);

        factory.setCommonErrorHandler(kafkaErrorHandler);

        factory.setConcurrency(3);

        return factory;
    }
}
