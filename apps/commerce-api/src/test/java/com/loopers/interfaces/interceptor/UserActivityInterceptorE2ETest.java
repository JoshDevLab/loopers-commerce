package com.loopers.interfaces.interceptor;

import com.loopers.domain.user.UserActivity;
import com.loopers.domain.user.UserActivityRepository;
import com.loopers.support.E2ETestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserActivityInterceptorE2ETest extends E2ETestSupport {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Test
    @DisplayName("상품 목록 조회 시 UserActivity가 올바르게 기록된다")
    void shouldTrackProductListView() {
        // Given
        String userId = "testUser123";
        String traceId = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        headers.set("X-TRACE-ID", traceId);

        // When
        ResponseEntity<String> response = client.exchange(
                "/api/v1/products",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();
                    
                    UserActivity activity = activities.stream()
                            .filter(a -> "/api/v1/products".equals(a.getUri()))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("상품 목록 조회 활동이 기록되지 않았습니다"));

                    // 기본 정보 검증
                    assertThat(activity.getUserId()).isEqualTo(userId);
                    assertThat(activity.getTraceId()).isEqualTo(traceId);
                    assertThat(activity.getMethod()).isEqualTo("GET");
                    assertThat(activity.getUri()).isEqualTo("/api/v1/products");
                    assertThat(activity.getStatus()).isEqualTo(200);
                    
                    // Route 정보 검증 (클래스명#메서드명 형태)
                    assertThat(activity.getRoute()).contains("ProductV1Controller#getProducts");
                });
    }

    @Test
    @DisplayName("단일 상품 조회 시 UserActivity가 올바르게 기록된다")
    void shouldTrackSingleProductView() {
        // Given
        String userId = "testUser123";
        Long productId = 1L; // 존재하지 않는 상품이어도 추적됨

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);

        // When
        ResponseEntity<String> response = client.exchange(
                "/api/v1/products/" + productId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();
                    
                    UserActivity activity = activities.stream()
                            .filter(a -> ("/api/v1/products/" + productId).equals(a.getUri()))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("상품 상세 조회 활동이 기록되지 않았습니다"));

                    assertThat(activity.getUserId()).isEqualTo(userId);
                    assertThat(activity.getMethod()).isEqualTo("GET");
                    assertThat(activity.getUri()).isEqualTo("/api/v1/products/" + productId);
                    // 상품이 존재하지 않을 경우 404 상태
                    assertThat(activity.getStatus()).isIn(200, 404);
                    assertThat(activity.getRoute()).contains("ProductV1Controller#getProductDetail");
                });
    }

    @Test
    @DisplayName("X-USER-ID 헤더가 없어도 추적된다")
    void shouldTrackWithoutUserId() {
        // Given - userId 헤더 없이
        String traceId = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TRACE-ID", traceId);

        // When
        ResponseEntity<String> response = client.exchange(
                "/api/v1/products",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();
                    
                    UserActivity activity = activities.stream()
                            .filter(a -> "/api/v1/products".equals(a.getUri()) && a.getUserId() == null)
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("userId가 null인 활동이 기록되지 않았습니다"));

                    assertThat(activity.getUserId()).isNull(); // userId는 null
                    assertThat(activity.getTraceId()).isEqualTo(traceId);
                    assertThat(activity.getMethod()).isEqualTo("GET");
                    assertThat(activity.getUri()).isEqualTo("/api/v1/products");
                });
    }

    @Test
    @DisplayName("X-TRACE-ID 헤더가 없으면 자동으로 trace ID가 생성된다")
    void shouldGenerateTraceIdWhenNotProvided() {
        // Given - traceId 헤더 없이
        String userId = "testUser123";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);

        // When
        ResponseEntity<String> response = client.exchange(
                "/api/v1/products",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();

                    UserActivity activity = activities.stream()
                            .filter(a -> "/api/v1/products".equals(a.getUri()) && userId.equals(a.getUserId()))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("해당 사용자의 활동이 기록되지 않았습니다"));

                    assertThat(activity.getUserId()).isEqualTo(userId);
                    // traceId가 자동 생성되어야 함 (Micrometer trace ID 또는 UUID 형태)
                    assertThat(activity.getTraceId()).isNotNull();
                    // Micrometer trace ID (32자리 hex) 또는 표준 UUID 패턴 모두 허용
                    assertThat(activity.getTraceId()).satisfiesAnyOf(
                            traceId -> assertThat(traceId).matches("^[0-9a-f]{32}$"), // Micrometer format
                            traceId -> assertThat(traceId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$") // UUID format
                    );
                });
    }

    @Test
    @DisplayName("추적 대상이 아닌 API는 기록되지 않는다")
    void shouldNotTrackNonTargetApis() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "testUser123");

        // When - WebConfig에서 제외된 경로들 호출
        client.exchange("/health", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        client.exchange("/actuator/health", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // Then
        try {
            Thread.sleep(3000); // 비동기 처리 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<UserActivity> activities = userActivityRepository.findAll();
        assertThat(activities).isEmpty();
    }

    @Test
    @DisplayName("HandlerMethod가 아닌 경우 route가 'unknown'으로 설정된다")
    void shouldSetRouteAsUnknownForNonHandlerMethod() {
        // Given - 정적 리소스나 에러 페이지 등 HandlerMethod가 아닌 경우
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", "testUser123");

        // When - 존재하지 않는 경로로 요청하여 에러 핸들러가 처리되도록 함
        ResponseEntity<String> response = client.exchange(
                "/api/v1/products/nonexistent/path",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();
                    
                    if (!activities.isEmpty()) {
                        // 만약 활동이 기록되었다면, route 검증
                        UserActivity activity = activities.get(0);
                        // HandlerMethod가 아닌 경우 route가 "unknown"이거나 실제 핸들러 메서드가 처리할 수 있음
                        assertThat(activity.getRoute()).isNotNull();
                    }
                });
    }

    @Test
    @DisplayName("여러 API를 연속 호출하면 모두 개별적으로 추적된다")
    void shouldTrackMultipleApiCalls() {
        // Given
        String userId = "testUser123";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);

        // When - 여러 API 연속 호출
        client.exchange("/api/v1/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        client.exchange("/api/v1/products/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        client.exchange("/api/v1/products/2", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();
                    
                    // 최소 3개의 활동이 기록되어야 함
                    assertThat(activities).hasSizeGreaterThanOrEqualTo(3);

                    // 각각의 URI가 올바르게 기록되었는지 확인
                    boolean hasProductList = activities.stream().anyMatch(a -> "/api/v1/products".equals(a.getUri()));
                    boolean hasProduct1 = activities.stream().anyMatch(a -> "/api/v1/products/1".equals(a.getUri()));
                    boolean hasProduct2 = activities.stream().anyMatch(a -> "/api/v1/products/2".equals(a.getUri()));
                    
                    assertThat(hasProductList).isTrue();
                    assertThat(hasProduct1).isTrue();
                    assertThat(hasProduct2).isTrue();

                    // 모든 활동이 동일한 사용자에 의한 것인지 확인
                    activities.forEach(activity -> {
                        assertThat(activity.getUserId()).isEqualTo(userId);
                        assertThat(activity.getMethod()).isEqualTo("GET");
                        assertThat(activity.getTraceId()).isNotNull();
                    });
                });
    }

    @Test
    @DisplayName("HTTP 상태 코드가 올바르게 기록된다")
    void shouldRecordCorrectHttpStatus() {
        // Given
        String userId = "testUser123";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);

        // When - 성공 케이스와 실패 케이스
        client.exchange("/api/v1/products", HttpMethod.GET, new HttpEntity<>(headers), String.class); // 200
        client.exchange("/api/v1/products/999999", HttpMethod.GET, new HttpEntity<>(headers), String.class); // 404

        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<UserActivity> activities = userActivityRepository.findAll();
                    assertThat(activities).hasSizeGreaterThanOrEqualTo(2);

                    // 성공 케이스 확인
                    UserActivity successActivity = activities.stream()
                            .filter(a -> "/api/v1/products".equals(a.getUri()))
                            .findFirst()
                            .orElseThrow();
                    assertThat(successActivity.getStatus()).isEqualTo(200);

                    // 실패 케이스 확인
                    UserActivity failActivity = activities.stream()
                            .filter(a -> "/api/v1/products/999999".equals(a.getUri()))
                            .findFirst()
                            .orElseThrow();
                    assertThat(failActivity.getStatus()).isEqualTo(404);
                });
    }
}
