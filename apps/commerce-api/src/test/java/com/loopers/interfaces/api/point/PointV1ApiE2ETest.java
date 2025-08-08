package com.loopers.interfaces.api.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.E2ETestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PointV1ApiE2ETest extends E2ETestSupport {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
    @Test
    void successGetPointThenReturnPoint() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        User user = userRepository.save(User.create(userId, email, birthday, gender));

        pointRepository.save(Point.create(BigDecimal.valueOf(10000), user.getId()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        ResponseEntity<ApiResponse<BigDecimal>> response = client.exchange(
                "/api/v1/points",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<BigDecimal>>() {
                }
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
    @Test
    void failGetMyInfo_whenNoHttpHeaders() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> httpEntityWithHeaders = new HttpEntity<>(headers);

        // Act
        ResponseEntity<ApiResponse<Long>> response = client.exchange(
                "/api/v1/points",
                HttpMethod.GET,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Long>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
    @Test
    void successChargePointThenReturnChargedPoint() {
        // Arrange
        String userId = "test123";
        String email = "email@email.com";
        String birthday = "1996-11-27";
        String gender = "MALE";
        User user = userRepository.save(User.create(userId, email, birthday, gender));
        pointRepository.save(Point.create(BigDecimal.valueOf(10000), user.getId()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", user.getUserId());
        HttpEntity<Long> httpEntityWithHeaders = new HttpEntity<>(1000L, headers);

        // Act
        ResponseEntity<ApiResponse<BigDecimal>> response = client.exchange(
                "/api/v1/points/charge",
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<BigDecimal>>() {
                }
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).isEqualByComparingTo(BigDecimal.valueOf(11000));
    }

    @DisplayName("존재하지 않는 유저가 포인트를 충전할 경우, 401 Unauthorized 응답을 반환한다.")
    @Test
    void failChargePoint_whenUserNotFound() {
        // Arrange
        String userId = "test123";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        HttpEntity<Long> httpEntityWithHeaders = new HttpEntity<>(1000L, headers);

        // Act
        ResponseEntity<ApiResponse<Long>> response = client.exchange(
                "/api/v1/points/charge",
                HttpMethod.POST,
                httpEntityWithHeaders,
                new ParameterizedTypeReference<ApiResponse<Long>>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


}
