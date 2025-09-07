package com.loopers.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final DockerImageName APACHE_KAFKA =
            DockerImageName.parse("apache/kafka:3.7.0"); // 또는 testcontainers 릴리즈 노트가 권장하는 태그

    public static final KafkaContainer KAFKA = new KafkaContainer(APACHE_KAFKA);

    static {
        KAFKA.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                // ★ 실제 애플리케이션이 사용할 부트스트랩 서버를 Testcontainers 카프카로 강제
                "spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),

                // 테스트 전용 토픽/그룹 오버라이드 (충돌 방지)
                "app.kafka.topics.product-like-events=product-like-events",
                "app.kafka.consumer-groups.product-like-collector=product-like-collector-e2e",

                // 리스너/컨슈머 설정(네가 쓰는 값 직렬화가 byte[] 이므로 그대로)
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer",
                "spring.kafka.listener.ack-mode=manual_immediate"
        ).applyTo(ctx.getEnvironment());
    }
}
