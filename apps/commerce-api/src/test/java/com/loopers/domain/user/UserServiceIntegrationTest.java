package com.loopers.domain.user;

import com.loopers.support.IntegrationTestSupport;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.loopers.support.InMemoryDbSupport.clearInMemoryStorage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    void reset() throws Exception {
        clearInMemoryStorage(userRepository);
    }

    @DisplayName("회원 가입시 User 저장이 수행된다.")
    @Test
    void userSignUp() {
        System.out.println("repository class = " + userRepository.getClass());
        System.out.println("is mock? = " + org.mockito.Mockito.mockingDetails(userRepository).isMock());
        System.out.println("is spy?  = " + org.mockito.Mockito.mockingDetails(userRepository).isSpy());

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
        User user = userService.signUp(userRegisterCommand);

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
        Gender gender = Gender.MALE;

        UserRegisterCommand userRegisterCommand1 = new UserRegisterCommand(
                userId,
                email,
                birthday,
                gender
        );

        UserRegisterCommand userRegisterCommand2 = new UserRegisterCommand(
                userId,
                email,
                birthday,
                gender
        );

        userService.signUp(userRegisterCommand1);

        // Act
        // Assert
        assertThatThrownBy(() -> userService.signUp(userRegisterCommand2))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.ALREADY_EXIST_USERID);

    }

}
