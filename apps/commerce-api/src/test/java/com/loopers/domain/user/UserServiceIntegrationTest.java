package com.loopers.domain.user;

import com.loopers.support.IntegrationTestSupport;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;


    @DisplayName("회원 가입시 User 저장이 수행된다.")
    @Test
    void userSignUp() {
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
        User user = userService.signUp(userCommand);

        // Assert
        assertThat(userRepository.findByUserId(user.getUserId())).isPresent();
    }

    @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
    @Test
    void alreadyExistIdFailSignUp() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";

        UserCommand.Register userCommand1 = new UserCommand.Register(
                userId,
                email,
                birthday,
                gender
        );

        UserCommand.Register userCommand2 = new UserCommand.Register(
                userId,
                email,
                birthday,
                gender
        );


        userService.signUp(userCommand1);

        // Act
        // Assert
        assertThatThrownBy(() -> userService.signUp(userCommand2))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.ALREADY_EXIST_USERID);

    }

    @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
    @Test
    void shouldReturnMemberInfo_whenMemberExistsById() {
        // Arrange
        User user = userRepository.save(User.create("test123", "email@email.com", "1996-11-27", "MALE"));

        // Act
        User myInfo = userService.getMyInfoByUserId(user.getUserId());

        // Assert
        assertThat(myInfo.getUserId()).isEqualTo(user.getUserId());
        assertThat(myInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(myInfo.getBirthDay()).isEqualTo(user.getBirthDay());
        assertThat(myInfo.getGender()).isEqualTo(user.getGender());
    }

}
