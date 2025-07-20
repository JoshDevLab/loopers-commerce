package com.loopers.application.userpoint;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPointFacadeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    UserPointFacade userPointFacade;

    @Autowired
    PointRepository pointRepository;

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
        UserInfo userInfo = userPointFacade.signUp(userCommand);

        // Assert
        Optional<Point> point = pointRepository.findByUserPk(userInfo.id());
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
        UserInfo userInfo = userPointFacade.signUp(userCommand);

        // Assert
        Optional<Point> point = pointRepository.findByUserPk(userInfo.id());
        assertThat(point).isPresent();
        assertThat(point).get().extracting(Point::getPointBalance).isEqualTo(0L);
    }

}
