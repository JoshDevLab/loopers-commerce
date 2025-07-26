package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointRepository;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PointFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    PointFacade pointFacade;

    @Autowired
    PointRepository pointRepository;

    @DisplayName("특정 userPk 에 대한 포인트 정보를 조회할 수 있다.")
    @Test
    void getPointByUserPk() {
        // Arrange
        Long userPk = 1L;
        BigDecimal pointBalance = BigDecimal.valueOf(10000);
        pointRepository.save(Point.create(pointBalance, userPk));

        // Act
        PointInfo pointInfo = pointFacade.getPoint(userPk);

        // Assert
        assertThat(pointInfo).isNotNull();
        assertThat(pointInfo.pointBalance()).isEqualByComparingTo(pointBalance);
    }

    @DisplayName("포인트 충전 후, 해당 userPk 의 포인트 잔액이 업데이트된다.")
    @Test
    void chargePointUpdatesBalance() {
        // Arrange
        Long userPk = 1L;
        BigDecimal initialBalance = BigDecimal.valueOf(10000);
        pointRepository.save(Point.create(initialBalance, userPk));
        BigDecimal chargeAmount = BigDecimal.valueOf(5000);

        // Act
        PointInfo updatedPointInfo = pointFacade.charge(userPk, chargeAmount);

        // Assert
        assertThat(updatedPointInfo).isNotNull();
        assertThat(updatedPointInfo.pointBalance()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }
}
