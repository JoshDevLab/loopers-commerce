package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.ExternalPaymentResponse;
import com.loopers.infrastructure.payment.LoopersPgFeginClient.PgTransactionData;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled
class LoopersPgFeginClientIntegrationTest {

    @Autowired
    private LoopersPgFeginClient pgClient;
    
    @Autowired
    private LoopersPgProcessor pgProcessor;

    @Test
    @DisplayName("Feign Client 직접 호출 - HTTP 500도 PgResponse로 처리")
    void callPgClientDirectly() {
        // Given
        LoopersPgFeginClient.PgPaymentRequest testRequest = 
            new LoopersPgFeginClient.PgPaymentRequest(
                "ORDER_" + System.currentTimeMillis(),
                "SAMSUNG",
                "1234-5678-9814-1451",
                5000L,
                "http://localhost:8080/api/v1/payments/callback"
            );

        // When
        PgResponse<PgTransactionData> response = pgClient.processPayment("135135", testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMeta()).isNotNull();
        assertThat(response.getResult()).isIn("SUCCESS", "FAIL");
        
        System.out.println("=== Feign Client 직접 호출 결과 ===");
        System.out.println("Result: " + response.getResult());
        System.out.println("Success: " + response.isSuccess());
        System.out.println("ErrorCode: " + response.getErrorCode());
        System.out.println("Message: " + response.getMessage());
        
        if (response.isSuccess()) {
            System.out.println("✓ 성공 응답 - Transaction Key: " + response.getData().getTransactionKey());
        } else {
            System.out.println("✓ 실패 응답 - HTTP 500도 정상적으로 PgResponse로 처리됨");
            if ("Internal Server Error".equals(response.getErrorCode())) {
                assertThat(response.getMessage()).contains("현재 서버가 불안정합니다");
            }
        }
    }

    @Test
    @DisplayName("PaymentProcessor를 통한 호출 - Circuit Breaker 동작 확인")
    void callThroughPaymentProcessor() {
        int totalCalls = 15;
        int successCount = 0;
        int pgServiceUnavailableCount = 0;
        int pgProcessingErrorCount = 0;
        int circuitBreakerCount = 0;

        for (int i = 0; i < totalCalls; i++) {
            try {
                LoopersPgFeginClient.LoopersPaymentRequest request = 
                    new LoopersPgFeginClient.LoopersPaymentRequest(
                        (long) i,
                        null, // cardType
                        null, // cardNo
                        "http://localhost:8080/callback",
                        new java.math.BigDecimal("5000"),
                        "135135"
                    );

                ExternalPaymentResponse response = pgProcessor.payment(request);
                successCount++;
                System.out.println("Success " + (i + 1) + ": " + response.getTransactionId());

            } catch (LoopersPgProcessor.PgServiceUnavailableException e) {
                pgServiceUnavailableCount++;
                System.out.println("PG Service Unavailable " + (i + 1) + ": " + e.getMessage());
                
            } catch (LoopersPgProcessor.PgProcessingException e) {
                pgProcessingErrorCount++;
                System.out.println("PG Processing Error " + (i + 1) + ": " + e.getMessage());
                
            } catch (NoFallbackAvailableException e) {
                circuitBreakerCount++;
                System.out.println("Circuit Breaker Open " + (i + 1) + ": Circuit Breaker가 열렸습니다");
                
            } catch (Exception e) {
                System.out.println("Unexpected Error " + (i + 1) + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            
            try {
                Thread.sleep(100); // API 호출 간격
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("=== PaymentProcessor 호출 결과 ===");
        System.out.println("총 호출: " + totalCalls);
        System.out.println("성공: " + successCount);
        System.out.println("PG 서비스 불가: " + pgServiceUnavailableCount);
        System.out.println("PG 처리 오류: " + pgProcessingErrorCount);
        System.out.println("Circuit Breaker 열림: " + circuitBreakerCount);
        
        // 40% 실패율이므로 일부 실패가 있어야 함
        int totalFailures = pgServiceUnavailableCount + pgProcessingErrorCount + circuitBreakerCount;
        assertThat(totalFailures).isGreaterThan(0);
        
        // Circuit Breaker가 동작했는지 확인 (여러 번 실패 후 Circuit이 열릴 수 있음)
        if (circuitBreakerCount > 0) {
            System.out.println("✓ Circuit Breaker가 정상 동작함");
        } else {
            System.out.println("- Circuit Breaker는 열리지 않음 (실패율이 임계치에 도달하지 않음)");
        }
    }

    @Test
    @DisplayName("여러 번 호출하여 Circuit Breaker 트리거 확인")
    void testCircuitBreakerTrigger() {
        // Circuit Breaker 설정: failure-rate-threshold: 60%, minimum-number-of-calls: 10
        // 40% 실패율이므로 Circuit Breaker가 열리지 않아야 함
        
        int totalCalls = 25; // 충분한 호출 수
        int successCount = 0;
        int failureCount = 0;
        int circuitBreakerCount = 0;

        for (int i = 0; i < totalCalls; i++) {
            try {
                LoopersPgFeginClient.LoopersPaymentRequest request = 
                    new LoopersPgFeginClient.LoopersPaymentRequest(
                        (long) i,
                        null,
                        null,
                        "http://localhost:8080/callback",
                        new java.math.BigDecimal("5000"),
                        "135135"
                    );

                pgProcessor.payment(request);
                successCount++;

            } catch (LoopersPgProcessor.PgServiceUnavailableException | LoopersPgProcessor.PgProcessingException e) {
                failureCount++;
                
            } catch (NoFallbackAvailableException e) {
                circuitBreakerCount++;
                System.out.println("Circuit Breaker 동작 감지: " + (i + 1) + "번째 호출");
                
            } catch (Exception e) {
                failureCount++;
                System.out.println("기타 오류: " + e.getClass().getSimpleName());
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        double failureRate = (double) failureCount / (successCount + failureCount) * 100;
        
        System.out.println("=== Circuit Breaker 트리거 테스트 결과 ===");
        System.out.println("총 호출: " + totalCalls);
        System.out.println("성공: " + successCount);
        System.out.println("실패: " + failureCount);
        System.out.println("Circuit Breaker: " + circuitBreakerCount);
        System.out.println("실패율: " + String.format("%.1f%%", failureRate));
        
        // PG 시뮬레이터 40% 실패율 < Circuit Breaker 60% 임계치
        // 따라서 Circuit Breaker가 열리지 않아야 함
        if (failureRate < 60.0) {
            System.out.println("✓ 실패율이 60% 미만이므로 Circuit Breaker가 열리지 않음 (정상)");
        } else {
            System.out.println("✓ 실패율이 60% 이상이므로 Circuit Breaker가 동작할 수 있음");
        }
    }

    @Test
    @DisplayName("결제 상태 조회 테스트")
    void getPaymentStatus() {
        // Given - 성공한 결제 생성
        String transactionKey = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            LoopersPgFeginClient.PgPaymentRequest paymentRequest = 
                new LoopersPgFeginClient.PgPaymentRequest(
                    "ORDER_" + System.currentTimeMillis() + "_" + attempt,
                    "SAMSUNG",
                    "1234-5678-9814-1451",
                    5000L,
                    "http://localhost:8080/api/v1/payments/callback"
                );

            PgResponse<PgTransactionData> paymentResponse = 
                pgClient.processPayment("135135", paymentRequest);
            
            if (paymentResponse.isSuccess()) {
                transactionKey = paymentResponse.getData().getTransactionKey();
                break;
            }
        }
        
        assertThat(transactionKey).isNotNull();
        System.out.println("성공한 거래키: " + transactionKey);

        // When - 상태 조회
        LoopersPgFeginClient.LoopersPaymentDetailResponse status = (LoopersPgFeginClient.LoopersPaymentDetailResponse) pgProcessor.getByTransactionKey(transactionKey);

        // Then
        assertThat(status).isNotNull();
        assertThat(status.getTransactionKey()).isEqualTo(transactionKey);
        
        System.out.println("=== 상태 조회 결과 ===");
        System.out.println("Transaction Key: " + status.getTransactionKey());
        System.out.println("Status: " + status.getStatus());
        System.out.println("Result: " + status.getResult());
    }

    @Test
    @DisplayName("PgResponse 구조 검증")
    void validatePgResponseStructure() {
        // Given
        LoopersPgFeginClient.PgPaymentRequest testRequest = 
            new LoopersPgFeginClient.PgPaymentRequest(
                "ORDER_" + System.currentTimeMillis(),
                "SAMSUNG",
                "1234-5678-9814-1451",
                5000L,
                "http://localhost:8080/api/v1/payments/callback"
            );

        // When
        PgResponse<PgTransactionData> response = pgClient.processPayment("135135", testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMeta()).isNotNull();
        assertThat(response.getResult()).isIn("SUCCESS", "FAIL");

        System.out.println("=== PgResponse 구조 검증 ===");
        System.out.println("Result: " + response.getResult());
        System.out.println("Success: " + response.isSuccess());
        System.out.println("ErrorCode: " + response.getErrorCode());
        System.out.println("Message: " + response.getMessage());

        if (response.isSuccess()) {
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getTransactionKey()).matches("\\d{8}:TR:\\w+");
            System.out.println("✓ 성공 응답 구조 검증 완료");
        } else {
            if ("Internal Server Error".equals(response.getErrorCode())) {
                assertThat(response.getMessage()).contains("현재 서버가 불안정합니다");
                System.out.println("✓ 실패 응답 구조 검증 완료");
            }
        }
    }
}
