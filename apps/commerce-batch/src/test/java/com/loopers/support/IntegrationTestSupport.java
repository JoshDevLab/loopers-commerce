package com.loopers.support;

import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBootTest
@SpringJUnitConfig({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
@TestPropertySource(properties = {
        "scheduling.enabled=false",
        "spring.batch.job.enabled=false"
})
public abstract class IntegrationTestSupport {
}
