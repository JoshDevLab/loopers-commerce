package com.loopers.scheduling.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스케줄링 설정 테스트
 */
@SpringBootTest(classes = {SchedulingConfig.class})
@TestPropertySource(properties = {
    "loopers.scheduling.pool-size=5",
    "loopers.scheduling.thread-name-prefix=test-scheduler-"
})
class SchedulingConfigTest {

    @Test
    void taskScheduler_shouldBeConfigured(TaskScheduler taskScheduler) {
        // given & when & then
        assertThat(taskScheduler).isNotNull();
    }
}
