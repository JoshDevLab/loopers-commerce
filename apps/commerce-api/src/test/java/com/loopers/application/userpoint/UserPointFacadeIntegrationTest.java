package com.loopers.application.userpoint;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointHistoryRepository;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.*;
import com.loopers.support.IntegrationTestSupport;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class UserPointFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    UserPointFacade userPointFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @DisplayName("회원가입이 성공하면 Point 0원이 부여된다.")
    @Test
    void successSignUpThenGivePointZero() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        UserCommand.Register userCommand = new UserCommand.Register(
                userId,
                email,
                birthday,
                gender
        );

        // Act
        userPointFacade.signUp(userCommand);

        // Assert
        Optional<Point> point = pointRepository.findByUserId(userId);
        assertThat(point).isPresent();
        assertThat(point).get().extracting(Point::getPointBalance).isEqualTo(0L);
    }

    @DisplayName("회원가입이 실패하면 Point 가 부여되지않는다")
    @Test
    void failedSignUpThenNotGivePoint() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        UserCommand.Register userCommand = new UserCommand.Register(
                userId,
                email,
                birthday,
                gender
        );

        // Act
        userPointFacade.signUp(userCommand);

        // Assert
        Optional<Point> point = pointRepository.findByUserId(userId);
        assertThat(point).isPresent();
        assertThat(point).get().extracting(Point::getPointBalance).isEqualTo(0L);
    }


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
        Point point = userPointFacade.existMemberGetPoint(userId);

        // Assert
        assertThat(point.getUserId()).isEqualTo(userId);
        assertThat(point.getPointBalance()).isEqualTo(10000L);
    }


    @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
    @Test
    void existUserIdThenReturnNull() {
        // Arrange
        String userId = "test123";
        pointRepository.save(Point.create(10000L, userId));

        // Act
        Point point = userPointFacade.existMemberGetPoint(userId);

        // Assert
        assertThat(point).isNull();
    }

    @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, CoreException ErrorType.USER_NOT_FOUND. 예외가 발생한다.")
    @Test
    public void chargingPointNotExistUserIdThenFailExistMemberCharging() {
        // Arrange
        String userId = "test123";
        Long chargePoint = 10000L;

        // Act
        CoreException exception = assertThrows(CoreException.class, () -> userPointFacade.existMemberChargingPoint(userId, chargePoint));

        // Assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.USER_NOT_FOUND);
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
        Point chargedPoint = userPointFacade.existMemberChargingPoint(userId, chargePoint);

        // Assert
        assertThat(chargedPoint).isNotNull();
        assertThat(chargedPoint.getUserId()).isEqualTo(userId);
        assertThat(chargedPoint.getPointBalance()).isEqualTo(15000L);
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
        Point point = userPointFacade.existMemberChargingPoint(userId, chargePoint);

        // Assert
        boolean result = pointHistoryRepository.existsByUserId(point.getUserId());
        assertThat(result).isTrue();
    }
}
