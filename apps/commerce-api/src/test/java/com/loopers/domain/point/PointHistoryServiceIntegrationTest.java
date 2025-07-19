package com.loopers.domain.point;

import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class PointHistoryServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private PointHistoryService pointHistoryService;

    @DisplayName("포인트 충전 이력이 올바르게 저장된다.")
    @Test
    void save_success() {
        // given
        String userId = "test123";
        Long point = 500L;

        // when
        PointHistoryInfo saved = pointHistoryService.save(userId, point);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved).isNotNull();
        assertThat(saved.userId()).isEqualTo(userId);
        assertThat(saved.point()).isEqualTo(point);
        assertThat(saved.type()).isEqualTo(PointHistoryType.CHARGE);
    }
}
