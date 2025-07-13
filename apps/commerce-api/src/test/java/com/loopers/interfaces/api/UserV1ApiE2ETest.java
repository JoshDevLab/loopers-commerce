package com.loopers.interfaces.api;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.user.dto.SignUpRequest;
import com.loopers.interfaces.api.user.dto.SignUpResponse;
import com.loopers.support.E2ETestSupport;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Objects;

import static com.loopers.support.InMemoryDbSupport.clearInMemoryStorage;
import static org.assertj.core.api.Assertions.assertThat;

public class UserV1ApiE2ETest extends E2ETestSupport {

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void reset() throws Exception {
        clearInMemoryStorage(userRepository);
    }

    @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
    @Test
    void successSignUpThenReturnUser() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        SignUpRequest request = new SignUpRequest(
                userId,
                email,
                birthday,
                gender
        );

        // Act
        ResponseEntity<ApiResponse<SignUpResponse>> response = client.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<ApiResponse<SignUpResponse>>() {}
        );

        // Assert
        ApiResponse<SignUpResponse> result = Objects.requireNonNull(response.getBody());

        assertThat(result.data().userId()).isEqualTo(userId);
        assertThat(result.data().email()).isEqualTo(email);
        assertThat(result.data().birthday()).isEqualTo(LocalDate.parse(birthday));
        assertThat(result.data().gender()).isEqualTo(Gender.valueOf(gender));
    }

    @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
    @Test
    void failSignUpNoGender() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "";
        SignUpRequest request = new SignUpRequest(
                userId,
                email,
                birthday,
                gender
        );

        // Act
        ResponseEntity<ApiResponse<SignUpResponse>> response = client.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<ApiResponse<SignUpResponse>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
