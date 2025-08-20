package com.loopers.infrastructure.payment.pg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.payment.CardType;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LoopersPgFeginClientIntegrationTest {

    @Autowired
    private LoopersPgFeginClient pgClient;
    
    @Autowired
    private ObjectMapper objectMapper;

    private String testUserId;
    private LoopersPgFeginClient.PgPaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        testUserId = "135135"; // PG 시뮬레이터에서 사용하는 기본 USER-ID
        
        // 테스트용 결제 요청 데이터
        paymentRequest = new LoopersPgFeginClient.PgPaymentRequest(
                "ORDER_" + System.currentTimeMillis(), // 고유한 주문 ID
                CardType.SAMSUNG.name(),
                "1234-5678-9012-3456",
                10000L,
                "http://localhost:8080/callback"
        );
    }

    @Test
    @DisplayName("PG 결제 요청 테스트 - 실제 PG 시뮬레이터 호출")
    void should_CallPgSimulator_When_ProcessPayment() throws Exception {
        // given
        System.out.println("=== PG 결제 요청 테스트 시작 ===");
        System.out.println("요청 데이터:");
        System.out.println("- OrderId: " + paymentRequest.orderId());
        System.out.println("- CardType: " + paymentRequest.cardType());
        System.out.println("- CardNo: " + paymentRequest.cardNo());
        System.out.println("- Amount: " + paymentRequest.amount());
        System.out.println("- CallbackUrl: " + paymentRequest.callbackUrl());
        System.out.println("- UserId: " + testUserId);
        System.out.println();

        try {
            // when: 실제 PG 시뮬레이터 호출
            PgResponse<LoopersPgFeginClient.PgTransactionResponse> response = 
                    pgClient.processPayment(testUserId, paymentRequest);

            // then: 응답 출력
            System.out.println("=== PG 응답 수신 ===");
            System.out.println("Raw Response JSON:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            System.out.println();

            System.out.println("=== 응답 분석 ===");
            System.out.println("Meta 정보:");
            System.out.println("- Result: " + response.getResult());
            System.out.println("- Success: " + response.isSuccess());
            System.out.println("- ErrorCode: " + response.getErrorCode());
            System.out.println("- Message: " + response.getMessage());
            System.out.println();

            if (response.getData() != null) {
                var data = response.getData();
                System.out.println("Data 정보:");
                System.out.println("- TransactionKey: " + data.getTransactionKey());
                System.out.println("- Status: " + data.getStatus());
                System.out.println("- Reason: " + data.getReason());
                System.out.println("- isPending(): " + data.isPending());
                System.out.println("- isSuccess(): " + data.isSuccess());
                System.out.println("- isFailed(): " + data.isFailed());
                
                // 결제 상태 조회도 테스트
                if (data.getTransactionKey() != null) {
                    testPaymentStatusCheck(data.getTransactionKey());
                }
            } else {
                System.out.println("Data: null");
            }

        } catch (Exception e) {
            System.out.println("=== 예외 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    @DisplayName("PG 결제 요청 - 여러 번 호출하여 확률적 응답 확인")
    void should_ShowVariousResponses_When_CallMultipleTimes() throws Exception {
        System.out.println("=== 여러 번 호출 테스트 (PG 시뮬레이터 확률적 응답 확인) ===");
        
        int successCount = 0;
        int failureCount = 0;
        int serverErrorCount = 0;
        
        for (int i = 1; i <= 10; i++) {
            System.out.println("\n--- " + i + "번째 호출 ---");
            
            // 매번 다른 주문 ID로 호출
            var request = new LoopersPgFeginClient.PgPaymentRequest(
                    "ORDER_" + System.currentTimeMillis() + "_" + i,
                    CardType.SAMSUNG.name(),
                    "1234-5678-9012-3456",
                    10000L,
                    "http://localhost:8080/callback"
            );
            
            try {
                PgResponse<LoopersPgFeginClient.PgTransactionResponse> response = 
                        pgClient.processPayment(testUserId, request);
                
                System.out.println("Result: " + response.getResult());
                if (response.getData() != null) {
                    System.out.println("Status: " + response.getData().getStatus());
                    System.out.println("Reason: " + response.getData().getReason());
                    
                    if (response.getData().isSuccess()) {
                        successCount++;
                    } else if (response.getData().isFailed()) {
                        failureCount++;
                    }
                } else {
                    System.out.println("Meta Error: " + response.getErrorCode() + " - " + response.getMessage());
                }
                
                // 요청 간 간격 (PG 시뮬레이터 부하 방지)
                Thread.sleep(100);
                
            } catch (Exception e) {
                serverErrorCount++;
                System.out.println("서버 오류: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }
        
        System.out.println("\n=== 결과 통계 ===");
        System.out.println("성공: " + successCount + "회");
        System.out.println("비즈니스 실패: " + failureCount + "회");
        System.out.println("서버 오류: " + serverErrorCount + "회");
        System.out.println("총 호출: 10회");
    }

    @Test
    @DisplayName("PG 결제 상태 조회 테스트")
    void should_GetPaymentStatus_When_ValidTransactionKey() throws Exception {
        // given: 먼저 결제 요청을 해서 TransactionKey를 얻음
        System.out.println("=== 1단계: 결제 요청 ===");
        PgResponse<LoopersPgFeginClient.PgTransactionResponse> paymentResponse = 
                pgClient.processPayment(testUserId, paymentRequest);
        
        if (paymentResponse.getData() == null || paymentResponse.getData().getTransactionKey() == null) {
            System.out.println("결제 요청 실패 - 상태 조회 테스트 건너뜀");
            return;
        }
        
        String transactionKey = paymentResponse.getData().getTransactionKey();
        System.out.println("TransactionKey: " + transactionKey);
        
        // when: 결제 상태 조회
        testPaymentStatusCheck(transactionKey);
    }

    @Test
    @DisplayName("PG 주문별 결제 조회 테스트")
    void should_GetPaymentsByOrderId_When_ValidOrderId() throws Exception {
        // given: 먼저 결제 요청을 해서 OrderId를 이용
        System.out.println("=== 1단계: 결제 요청 ===");
        PgResponse<LoopersPgFeginClient.PgTransactionResponse> paymentResponse = 
                pgClient.processPayment(testUserId, paymentRequest);
        
        System.out.println("결제 요청 결과: " + paymentResponse.getResult());
        
        // when: 주문별 결제 조회
        System.out.println("\n=== 2단계: 주문별 결제 조회 ===");
        try {
            PgResponse<LoopersPgFeginClient.PgOrderResponse> orderResponse = 
                    pgClient.getPaymentsByOrderId(testUserId, paymentRequest.orderId());
            
            // then: 응답 출력
            System.out.println("=== 주문별 결제 조회 응답 ===");
            System.out.println("Raw Response JSON:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(orderResponse));
            System.out.println();
            
            System.out.println("Meta 정보:");
            System.out.println("- Result: " + orderResponse.getResult());
            System.out.println("- Success: " + orderResponse.isSuccess());
            
            if (orderResponse.getData() != null) {
                var data = orderResponse.getData();
                System.out.println("주문 정보:");
                System.out.println("- OrderId: " + data.getOrderId());
                System.out.println("- Transactions Count: " + (data.getTransactions() != null ? data.getTransactions().size() : 0));
                
                if (data.getTransactions() != null) {
                    for (int i = 0; i < data.getTransactions().size(); i++) {
                        var tx = data.getTransactions().get(i);
                        System.out.println("  Transaction[" + i + "]:");
                        System.out.println("    - TransactionKey: " + tx.getTransactionKey());
                        System.out.println("    - Status: " + tx.getStatus());
                        System.out.println("    - Reason: " + tx.getReason());
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("주문별 조회 오류: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("PG 입력 검증 오류 테스트 - 잘못된 카드번호")
    void should_ReturnValidationError_When_InvalidCardNumber() throws Exception {
        // given: 잘못된 카드번호 형식
        var invalidRequest = new LoopersPgFeginClient.PgPaymentRequest(
                "ORDER_" + System.currentTimeMillis(),
                CardType.SAMSUNG.name(),
                "invalid-card-number", // 잘못된 형식
                10000L,
                "http://localhost:8080/callback"
        );
        
        System.out.println("=== 입력 검증 오류 테스트 ===");
        System.out.println("잘못된 카드번호: " + invalidRequest.cardNo());
        
        try {
            // when: PG 호출
            PgResponse<LoopersPgFeginClient.PgTransactionResponse> response = 
                    pgClient.processPayment(testUserId, invalidRequest);
            
            System.out.println("예상과 다르게 성공 응답 받음:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            
        } catch (Exception e) {
            // then: 예외 발생 예상
            System.out.println("=== 예상된 검증 오류 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("예외 메시지: " + e.getMessage());
        }
    }

    /**
     * 결제 상태 조회 헬퍼 메서드
     */
    private void testPaymentStatusCheck(String transactionKey) throws Exception {
        System.out.println("\n=== 결제 상태 조회 테스트 ===");
        System.out.println("TransactionKey: " + transactionKey);
        
        try {
            PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> statusResponse = 
                    pgClient.getPaymentStatus(testUserId, transactionKey);
            
            System.out.println("=== 상태 조회 응답 ===");
            System.out.println("Raw Response JSON:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(statusResponse));
            System.out.println();
            
            System.out.println("Meta 정보:");
            System.out.println("- Result: " + statusResponse.getResult());
            System.out.println("- Success: " + statusResponse.isSuccess());
            
            if (statusResponse.getData() != null) {
                var data = statusResponse.getData();
                System.out.println("상세 정보:");
                System.out.println("- TransactionKey: " + data.getTransactionKey());
                System.out.println("- OrderId: " + data.getOrderId());
                System.out.println("- CardType: " + data.getCardType());
                System.out.println("- CardNo: " + data.getCardNo());
                System.out.println("- Amount: " + data.getAmount());
                System.out.println("- Status: " + data.getStatus());
                System.out.println("- Reason: " + data.getReason());
                System.out.println("- ProcessedAt: " + data.getProcessedAt());
                System.out.println("- isSuccess(): " + data.isSuccess());
                System.out.println("- isFailed(): " + data.isFailed());
            }
            
        } catch (Exception e) {
            System.out.println("상태 조회 오류: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
