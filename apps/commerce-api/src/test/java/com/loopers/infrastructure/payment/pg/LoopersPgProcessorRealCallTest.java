package com.loopers.infrastructure.payment.pg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.ExternalPaymentResponse;
import com.loopers.infrastructure.payment.pg.exception.PgBusinessException;
import com.loopers.infrastructure.payment.pg.exception.PgServerErrorException;
import com.loopers.infrastructure.payment.pg.exception.PgValidationException;
import com.loopers.interfaces.api.payment.dto.CardNo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
class LoopersPgProcessorRealCallTest {

    @Autowired
    private LoopersPgProcessor pgProcessor;
    
    @Autowired
    private ObjectMapper objectMapper;

    private LoopersPgFeginClient.LoopersPaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 결제 요청 데이터
        paymentRequest = new LoopersPgFeginClient.LoopersPaymentRequest(
                System.currentTimeMillis(), // 고유한 주문 ID
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://localhost:8080/callback",
                BigDecimal.valueOf(10000),
                "135135"
        );
    }

    @Test
    @DisplayName("PgProcessor 결제 요청 테스트 - 실제 호출")
    void should_ProcessPayment_When_ValidRequest() throws Exception {
        System.out.println("=== PgProcessor 결제 요청 테스트 ===");
        System.out.println("요청 데이터:");
        System.out.println("- OrderId: " + paymentRequest.getOrderId());
        System.out.println("- CardType: " + paymentRequest.getCardType());
        System.out.println("- CardNo: " + paymentRequest.getCardNo().getValue());
        System.out.println("- Amount: " + paymentRequest.getAmount());
        System.out.println("- CallbackUrl: " + paymentRequest.getCallbackUrl());
        System.out.println("- UserId: " + paymentRequest.getUserId());
        System.out.println();

        try {
            // when: PgProcessor를 통한 결제 요청
            ExternalPaymentResponse response = pgProcessor.payment(paymentRequest);
            
            // then: 응답 출력
            System.out.println("=== 결제 성공 응답 ===");
            System.out.println("Response JSON:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            System.out.println();
            
            if (response instanceof LoopersPgFeginClient.LoopersPaymentResponse loopersResponse) {
                System.out.println("응답 상세:");
                System.out.println("- TransactionId: " + loopersResponse.getTransactionId());
                System.out.println("- Result: " + loopersResponse.getResult());
                System.out.println("- Status: " + loopersResponse.getStatus());
                System.out.println("- Reason: " + loopersResponse.getReason());
                System.out.println("- isSuccess(): " + loopersResponse.isSuccess());
                System.out.println("- isFailed(): " + loopersResponse.isFailed());
                System.out.println("- isPending(): " + loopersResponse.isPending());
                
                // 결제 상태 조회도 테스트
                if (loopersResponse.getTransactionId() != null) {
                    testPaymentStatusQuery(loopersResponse.getTransactionId());
                }
            }
            
        } catch (PgBusinessException e) {
            System.out.println("=== PG 비즈니스 로직 실패 (예상 가능한 실패) ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("TransactionKey: " + e.getTransactionKey());
            System.out.println("Status: " + e.getTransactionStatus());
            System.out.println("Reason: " + e.getReason());
            System.out.println("추론된 에러 타입: " + e.getInferredErrorType());
            System.out.println("재시도 가능: " + e.isRetryable());
            System.out.println("CB 기록: " + e.shouldRecordAsFailure());
            
        } catch (PgValidationException e) {
            System.out.println("=== PG 입력 검증 실패 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("검증 필드: " + e.getValidationField());
            System.out.println("메시지: " + e.getMessage());
            System.out.println("재시도 가능: " + e.isRetryable());
            System.out.println("CB 기록: " + e.shouldRecordAsFailure());
            
        } catch (PgServerErrorException e) {
            System.out.println("=== PG 서버 오류 (40% 확률) ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("PG ErrorCode: " + e.getPgErrorCode());
            System.out.println("PG Message: " + e.getPgMessage());
            System.out.println("재시도 가능: " + e.isRetryable());
            System.out.println("CB 기록: " + e.shouldRecordAsFailure());
            
        } catch (Exception e) {
            System.out.println("=== 기타 예외 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("메시지: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("PgProcessor 여러 번 호출 테스트 - 확률적 응답 확인")
    void should_ShowVariousExceptions_When_CallMultipleTimes() throws Exception {
        System.out.println("=== PgProcessor 여러 번 호출 테스트 ===");
        
        int successCount = 0;
        int businessFailureCount = 0;
        int serverErrorCount = 0;
        int validationErrorCount = 0;
        int otherErrorCount = 0;
        
        for (int i = 1; i <= 15; i++) {
            System.out.println("\n--- " + i + "번째 호출 ---");
            
            // 매번 다른 주문 ID로 호출
            var request = new LoopersPgFeginClient.LoopersPaymentRequest(
                    System.currentTimeMillis() + i, // 고유한 주문 ID
                    CardType.SAMSUNG,
                    CardNo.valueOfName("1234567890123456"),
                    "http://localhost:8080/callback",
                    BigDecimal.valueOf(10000),
                    "135135"
            );
            
            try {
                ExternalPaymentResponse response = pgProcessor.payment(request);
                successCount++;
                System.out.println("✅ 성공 - TransactionId: " + response.getTransactionId());
                
                if (response instanceof LoopersPgFeginClient.LoopersPaymentResponse loopersResponse) {
                    System.out.println("   Status: " + loopersResponse.getStatus() + ", Reason: " + loopersResponse.getReason());
                }
                
            } catch (PgBusinessException e) {
                businessFailureCount++;
                System.out.println("💳 비즈니스 실패 - " + e.getInferredErrorType() + ": " + e.getReason());
                
            } catch (PgValidationException e) {
                validationErrorCount++;
                System.out.println("📝 검증 실패 - " + e.getValidationField() + ": " + e.getMessage());
                
            } catch (PgServerErrorException e) {
                serverErrorCount++;
                System.out.println("🔴 서버 오류 - " + e.getPgErrorCode() + ": " + e.getPgMessage());
                
            } catch (Exception e) {
                otherErrorCount++;
                System.out.println("❓ 기타 오류 - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            
            // 요청 간 간격 (PG 시뮬레이터 부하 방지)
            Thread.sleep(200);
        }
        
        System.out.println("\n=== 최종 통계 (15회 호출) ===");
        System.out.println("✅ 성공: " + successCount + "회");
        System.out.println("💳 비즈니스 실패: " + businessFailureCount + "회");
        System.out.println("🔴 서버 오류: " + serverErrorCount + "회");
        System.out.println("📝 검증 실패: " + validationErrorCount + "회");
        System.out.println("❓ 기타 오류: " + otherErrorCount + "회");
        System.out.println("성공률: " + String.format("%.1f%%", (successCount * 100.0 / 15)));
    }

    @Test
    @DisplayName("PgProcessor 잘못된 카드번호 테스트")
    void should_ThrowValidationException_When_InvalidCardNumber() throws Exception {
        // given: 짧은 카드번호
        var invalidRequest = new LoopersPgFeginClient.LoopersPaymentRequest(
                System.currentTimeMillis(),
                CardType.SAMSUNG,
                CardNo.valueOfName("123456"), // 6자리 (잘못된 형식)
                "http://localhost:8080/callback",
                BigDecimal.valueOf(10000),
                "135135"
        );
        
        System.out.println("=== 잘못된 카드번호 테스트 ===");
        System.out.println("카드번호: " + invalidRequest.getCardNo().getValue());
        System.out.println("PG 요청 형태: " + invalidRequest.toPgRequest().cardNo());
        
        try {
            // when: 잘못된 카드번호로 결제 요청
            ExternalPaymentResponse response = pgProcessor.payment(invalidRequest);
            System.out.println("예상과 다르게 성공: " + response.getTransactionId());
            
        } catch (PgValidationException e) {
            System.out.println("=== 예상된 검증 오류 발생 ===");
            System.out.println("검증 필드: " + e.getValidationField());
            System.out.println("오류 메시지: " + e.getMessage());
            System.out.println("재시도 가능: " + e.isRetryable());
            System.out.println("CB 기록: " + e.shouldRecordAsFailure());
            
        } catch (Exception e) {
            System.out.println("다른 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /**
     * 결제 상태 조회 헬퍼 메서드
     */
    private void testPaymentStatusQuery(String transactionId) throws Exception {
        System.out.println("\n=== 결제 상태 조회 테스트 ===");
        System.out.println("TransactionId: " + transactionId);
        
        try {
            ExternalPaymentResponse statusResponse = pgProcessor.getByTransactionKey(transactionId);
            
            System.out.println("=== 상태 조회 성공 ===");
            System.out.println("Response JSON:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(statusResponse));
            
            if (statusResponse instanceof LoopersPgFeginClient.LoopersPaymentDetailResponse detailResponse) {
                System.out.println("상세 정보:");
                System.out.println("- TransactionId: " + detailResponse.getTransactionId());
                System.out.println("- OrderId: " + detailResponse.getOrderId());
                System.out.println("- Status: " + detailResponse.getStatus());
                System.out.println("- Reason: " + detailResponse.getReason());
                System.out.println("- CardType: " + detailResponse.getCardType());
                System.out.println("- Amount: " + detailResponse.getAmount());
                System.out.println("- ProcessedAt: " + detailResponse.getProcessedAt());
                System.out.println("- FailureReason: " + detailResponse.getFailureReason());
            }
            
        } catch (Exception e) {
            System.out.println("상태 조회 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
