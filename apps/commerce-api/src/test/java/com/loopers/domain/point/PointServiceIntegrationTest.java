package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.IntegrationTestSupport;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class PointServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    PointService pointService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
    @Test
    void existUserIdThenReturnPoint() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        User user = userRepository.save(User.create(userId, email, birthday, gender));

        pointRepository.save(Point.create(10000L, user.getUserId()));

        // Act
        PointInfo point = pointService.getPoint(userId);

        // Assert
        assertThat(point.userId()).isEqualTo(userId);
        assertThat(point.pointBalance()).isEqualTo(10000L);
    }

    @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, CoreException ErrorType.POINT_NOT_FOUND. 예외가 발생한다.")
    @Test
    public void chargingPointNotExistUserIdThenFailExistMemberCharging() {
        // Arrange
        String userId = "test123";
        Long chargePoint = 10000L;

        // Act
        CoreException exception = assertThrows(CoreException.class, () -> pointService.charge(userId, chargePoint));

        // Assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.POINT_NOT_FOUND);
    }

    @DisplayName("존재하는 유저 ID 로 충전을 시도한 경우 보유 포인트가 증가한다.")
    @Test
    void existMemberChargingPointThenHavingPointIncrease() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";

        User user = userRepository.save(User.create(userId, email, birthday, gender));
        pointRepository.save(Point.create(10000L, user.getUserId()));
        Long chargePoint = 5000L;

        // Act
        PointInfo chargedPoint = pointService.charge(userId, chargePoint);

        // Assert
        assertThat(chargedPoint).isNotNull();
        assertThat(chargedPoint.userId()).isEqualTo(userId);
        assertThat(chargedPoint.pointBalance()).isEqualTo(15000L);
    }

    @DisplayName("존재하는 유저 ID 로 충전을 시도한 경우 포인트 히스토리에 충전이력이 저장된다.")
    @Test
    void existMemberChargingPointThenPointHistorySave() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";

        User user = userRepository.save(User.create(userId, email, birthday, gender));
        pointRepository.save(Point.create(10000L, user.getUserId()));
        Long chargePoint = 5000L;

        // Act
        PointInfo point = pointService.charge(userId, chargePoint);

        // Assert
        boolean result = pointHistoryRepository.existsByUserId(point.userId());
        assertThat(result).isTrue();
    }
}
