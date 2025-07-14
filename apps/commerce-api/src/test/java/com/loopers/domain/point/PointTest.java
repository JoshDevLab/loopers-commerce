package com.loopers.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

}
