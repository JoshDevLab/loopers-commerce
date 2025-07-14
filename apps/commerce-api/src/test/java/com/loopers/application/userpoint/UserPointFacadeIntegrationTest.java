package com.loopers.application.userpoint;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.*;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.loopers.support.InMemoryDbSupport.clearInMemoryStorage;
import static org.assertj.core.api.Assertions.assertThat;

public class UserPointFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    UserPointFacade userPointFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @BeforeEach
    void reset() throws Exception {
        clearInMemoryStorage(userRepository);
        clearInMemoryStorage(pointRepository);
    }

    @DisplayName("회원가입이 성공하면 Point 0원이 부여된다.")
    @Test
    void successSignUpThenGivePointZero() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        Gender gender = Gender.MALE;
        UserRegisterCommand userRegisterCommand = new UserRegisterCommand(
                userId,
                email,
                birthday,
                gender
        );

        // Act
        userPointFacade.signUp(userRegisterCommand);

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
        Gender gender = Gender.MALE;
        UserRegisterCommand userRegisterCommand = new UserRegisterCommand(
                userId,
                email,
                birthday,
                gender
        );

        // Act
        userPointFacade.signUp(userRegisterCommand);

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
        Gender gender = Gender.MALE;
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
}
