package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTest {

    @DisplayName("Point 를 초기화 생성할때 인자로 받은 userPk 가 저장되고 포인트잔액은 0원이다.")
    @Test
    void pointInitCreate() {
        // Arrange
        Long userPk = 1L;

        // Act
        Point point = Point.createInit(userPk);

        // Assert
        assertThat(point.getUserPk()).isEqualTo(userPk);
        assertThat(point.getPointBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100", "-50000"})
    void whenChargeTenPointThenFailCharging(String chargePoint) {
        // Arrange
        Point point = Point.createInit(1L);
        BigDecimal chargePointValue = new BigDecimal(chargePoint);

        // Act
        CoreException exception = assertThrows(CoreException.class, () -> point.charge(chargePointValue));

        // Assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.POINT_CHARGING_ERROR);
    }

}
