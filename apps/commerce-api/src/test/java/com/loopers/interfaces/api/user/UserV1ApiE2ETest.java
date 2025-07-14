package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.MyInfoResponse;
import com.loopers.interfaces.api.user.dto.SignUpRequest;
import com.loopers.interfaces.api.user.dto.SignUpResponse;
import com.loopers.support.E2ETestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

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
        SignUpResponse data = Objects.requireNonNull(response.getBody()).data();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.userId()).isEqualTo(userId);
        assertThat(data.email()).isEqualTo(email);
        assertThat(data.birthday()).isEqualTo(LocalDate.parse(birthday));
        assertThat(data.gender()).isEqualTo(Gender.valueOf(gender));
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

    @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
    @Test
    void successGetMyInfo_thenReturnMyInfo() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        User user = userRepository.save(User.create(userId, email, birthday, Gender.MALE));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        ResponseEntity<ApiResponse<MyInfoResponse>> response = client.exchange(
                "/api/v1/users/me",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<MyInfoResponse>>() {
                }
        );

        // Assert
        MyInfoResponse data = Objects.requireNonNull(response.getBody()).data();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.userId()).isEqualTo(userId);
        assertThat(data.email()).isEqualTo(email);
        assertThat(data.birthday()).isEqualTo(LocalDate.parse(birthday));
        assertThat(data.gender()).isEqualTo(Gender.valueOf(gender));

    }

    @DisplayName("/api/v1/users/me 엔드포인트 호출 시 X-USER-ID HTTP 헤더가 없으면 400 에러를 반환한다.")
    @Test
    void failGetMyInfo_whenNoHttpHeaders() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        ResponseEntity<ApiResponse<MyInfoResponse>> response = client.exchange(
                "/api/v1/users/me",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<MyInfoResponse>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("존재하지 않는 USER ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
    @Test
    void failGetMyInfo_whenNotExistsUserId() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "NON_EXIST_USER_ID");
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        ResponseEntity<ApiResponse<MyInfoResponse>> response = client.exchange(
                "/api/v1/users/me",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<MyInfoResponse>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
