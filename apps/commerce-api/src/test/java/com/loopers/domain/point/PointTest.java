package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTest {

    @DisplayName("Point 를 초기화 생성할때 인자로 받은 userId가 지정되고 포인트잔액은 0원이다.")
    @Test
    void pointInitCreate() {
        // Arrange
        String userId = "test123";

        // Act
        Point point = Point.createInit(userId);

        // Assert
        assertThat(point.getUserId()).isEqualTo(userId);
        assertThat(point.getPointBalance()).isEqualTo(0L);
    }

    @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100, Long.MIN_VALUE, -50000})
    void whenChargeTenPointThenFailCharging(Long chargePoint) {
        // Arrange
        Point point = Point.createInit("test123");

        // Act
        CoreException exception = assertThrows(CoreException.class, () -> point.charge(chargePoint));

        // Assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.POINT_CHARGING_ERROR);
    }

}
