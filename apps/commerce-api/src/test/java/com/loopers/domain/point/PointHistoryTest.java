package com.loopers.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryTest {

    @Test
    @DisplayName("정적 팩토리 메서드 create()로 올바르게 생성된다")
    void createPointHistory_success() {
        // given
        Long userPk = 1L;
        BigDecimal point = BigDecimal.valueOf(100);
        PointHistoryType type = PointHistoryType.CHARGE;

        // when
        PointHistory result = PointHistory.create(userPk, point, type);

        // then
        assertThat(result.getUserPk()).isEqualTo(userPk);
        assertThat(result.getPoint()).isEqualTo(point);
        assertThat(result.getType()).isEqualTo(type);
    }
}
