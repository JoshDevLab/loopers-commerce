package com.loopers.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryTest {

    @Test
    @DisplayName("정적 팩토리 메서드 create()로 올바르게 생성된다")
    void createPointHistory_success() {
        // given
        String userId = "test123";
        Long point = 100L;
        PointHistoryType type = PointHistoryType.CHARGE;

        // when
        PointHistory result = PointHistory.create(userId, point, type);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getPoint()).isEqualTo(point);
        assertThat(result.getType()).isEqualTo(type);
    }
}
