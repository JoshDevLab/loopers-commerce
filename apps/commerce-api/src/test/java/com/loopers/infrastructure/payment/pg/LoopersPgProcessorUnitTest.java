package com.loopers.infrastructure.payment.pg;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.ExternalPaymentRequest;
import com.loopers.domain.payment.ExternalPaymentResponse;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.infrastructure.payment.pg.exception.PgBusinessException;
import com.loopers.infrastructure.payment.pg.exception.PgGeneralException;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import com.loopers.interfaces.api.payment.dto.CardNo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.loopers.domain.payment.Payment.PaymentType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * LoopersPgProcessor 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class LoopersPgProcessorUnitTest {

    @Mock
    private LoopersPgFeginClient client;

    @InjectMocks
    private LoopersPgProcessor processor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(processor, "userId", "test-user-123");
    }

    @Nested
    @DisplayName("getPaymentType 메서드")
    class GetPaymentType {

        @Test
        @DisplayName("CARD 타입을 반환한다")
        void shouldReturnCardType() {
            // when
            PaymentType result = processor.getPaymentType();

            // then
            assertThat(result).isEqualTo(PaymentType.CARD);
        }
    }

    @Nested
    @DisplayName("createRequest 메서드")
    class CreateRequest {

        @Test
        @DisplayName("PaymentCommand.Request로부터 LoopersPaymentRequest를 생성한다")
        void shouldCreateLoopersPaymentRequest() {
            // given
            PaymentCommand.Request paymentCommand = new PaymentCommand.Request(
                100L,
                PaymentType.CARD,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url"
            );
            BigDecimal paidAmount = BigDecimal.valueOf(10000);

            // when
            ExternalPaymentRequest result = processor.createRequest(paymentCommand, paidAmount);

            // then
            assertThat(result).isInstanceOf(LoopersPgFeginClient.LoopersPaymentRequest.class);
            LoopersPgFeginClient.LoopersPaymentRequest request = 
                (LoopersPgFeginClient.LoopersPaymentRequest) result;
            
            assertThat(request.getOrderId()).isEqualTo(100L);
            assertThat(request.getCardType()).isEqualTo(CardType.SAMSUNG);
            assertThat(request.getAmount()).isEqualTo(paidAmount);
            assertThat(request.getCallbackUrl()).isEqualTo("http://callback.url");
            // ReflectionTestUtils가 제대로 적용되었는지 확인
            assertThat(request.getUserId()).isEqualTo("test-user-123");
        }
    }

    @Nested
    @DisplayName("payment 메서드")
    class Payment {

        private LoopersPgFeginClient.LoopersPaymentRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new LoopersPgFeginClient.LoopersPaymentRequest(
                100L,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url",
                BigDecimal.valueOf(10000),
                "test-user-123"
            );
        }

        @Test
        @DisplayName("성공적인 결제 요청 시 LoopersPaymentResponse를 반환한다")
        void shouldReturnSuccessResponseWhenPaymentSucceeds() {
            // given
            LoopersPgFeginClient.PgTransactionResponse pgData = createPgTransactionResponse(
                "txn-123", "SUCCESS", null
            );
            PgResponse<LoopersPgFeginClient.PgTransactionResponse> pgResponse = 
                createSuccessPgResponse(pgData);

            given(client.processPayment(anyString(), any())).willReturn(pgResponse);

            // when
            ExternalPaymentResponse result = processor.payment(validRequest);

            // then
            assertThat(result).isInstanceOf(LoopersPgFeginClient.LoopersPaymentResponse.class);
            LoopersPgFeginClient.LoopersPaymentResponse response = 
                (LoopersPgFeginClient.LoopersPaymentResponse) result;
            
            assertThat(response.getTransactionId()).isEqualTo("txn-123");
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("PG 비즈니스 로직 실패 시 PgBusinessException을 던진다")
        void shouldThrowPgBusinessExceptionWhenPgBusinessLogicFails() {
            // given
            LoopersPgFeginClient.PgTransactionResponse pgData = createPgTransactionResponse(
                "txn-123", "FAILED", "Invalid card number"
            );
            PgResponse<LoopersPgFeginClient.PgTransactionResponse> pgResponse = 
                createSuccessPgResponse(pgData);

            given(client.processPayment(anyString(), any())).willReturn(pgResponse);

            // when & then
            assertThatThrownBy(() -> processor.payment(validRequest))
                .isInstanceOf(PgBusinessException.class)
                .hasMessageContaining("Invalid card number");
        }

        @Test
        @DisplayName("잘못된 요청 타입 시 CoreException을 던진다")
        void shouldThrowCoreExceptionWhenInvalidRequestType() {
            // given
            ExternalPaymentRequest invalidRequest = new ExternalPaymentRequest(BigDecimal.valueOf(10000)) {};

            // when & then
            assertThatThrownBy(() -> processor.payment(invalidRequest))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_PAYMENT_REQUEST_TYPE);
        }

        @Test
        @DisplayName("PG API 실패 응답 시 PgGeneralException을 던진다")
        void shouldThrowPgGeneralExceptionWhenPgApiFails() {
            // given
            PgResponse<LoopersPgFeginClient.PgTransactionResponse> pgResponse = 
                createFailPgResponse("INVALID_REQUEST", "잘못된 요청입니다");

            given(client.processPayment(anyString(), any())).willReturn(pgResponse);

            // when & then
            assertThatThrownBy(() -> processor.payment(validRequest))
                .isInstanceOf(PgGeneralException.class)
                .hasMessageContaining("PG API 응답 실패");
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 PgProcessingException을 던진다")
        void shouldThrowPgProcessingExceptionWhenUnexpectedErrorOccurs() {
            // given
            given(client.processPayment(anyString(), any()))
                .willThrow(new RuntimeException("Unexpected error"));

            // when & then
            assertThatThrownBy(() -> processor.payment(validRequest))
                .isInstanceOf(LoopersPgProcessor.PgProcessingException.class)
                .hasMessageContaining("결제 처리 중 예상치 못한 오류 발생");
        }
    }

    @Nested
    @DisplayName("getByTransactionKey 메서드")
    class GetByTransactionKey {

        @Test
        @DisplayName("성공적인 상태 조회 시 LoopersPaymentDetailResponse를 반환한다")
        void shouldReturnDetailResponseWhenStatusCheckSucceeds() {
            // given
            String transactionKey = "txn-123";
            LoopersPgFeginClient.PgTransactionDetailResponse pgData = createPgTransactionDetailResponse(
                transactionKey, "order-100", "SUCCESS", null, 10000L
            );
            PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> pgResponse = 
                createSuccessDetailPgResponse(pgData);

            given(client.getPaymentStatus(anyString(), anyString())).willReturn(pgResponse);

            // when
            ExternalPaymentResponse result = processor.getByTransactionKey(transactionKey);

            // then
            assertThat(result).isInstanceOf(LoopersPgFeginClient.LoopersPaymentDetailResponse.class);
            LoopersPgFeginClient.LoopersPaymentDetailResponse response = 
                (LoopersPgFeginClient.LoopersPaymentDetailResponse) result;
            
            assertThat(response.getTransactionId()).isEqualTo(transactionKey);
            assertThat(response.getOrderId()).isEqualTo("order-100");
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        }

        @Test
        @DisplayName("PG API 실패 응답 시 PgGeneralException을 던진다")
        void shouldThrowPgGeneralExceptionWhenStatusCheckFails() {
            // given
            String transactionKey = "txn-123";
            PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> pgResponse = 
                createFailDetailPgResponse("NOT_FOUND", "거래를 찾을 수 없습니다");

            given(client.getPaymentStatus(anyString(), anyString())).willReturn(pgResponse);

            // when & then
            assertThatThrownBy(() -> processor.getByTransactionKey(transactionKey))
                .isInstanceOf(PgGeneralException.class);
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 PgProcessingException을 던진다")
        void shouldThrowPgProcessingExceptionWhenUnexpectedErrorOccurs() {
            // given
            String transactionKey = "txn-123";
            given(client.getPaymentStatus(anyString(), anyString()))
                .willThrow(new RuntimeException("Network error"));

            // when & then
            assertThatThrownBy(() -> processor.getByTransactionKey(transactionKey))
                .isInstanceOf(LoopersPgProcessor.PgProcessingException.class)
                .hasMessageContaining("결제 상태 조회 중 예상치 못한 오류 발생");
        }
    }

    @Nested
    @DisplayName("폴백 메서드")
    class FallbackMethods {

        @Test
        @DisplayName("결제 폴백 시 PgServiceTemporarilyUnavailableException을 던진다")
        void shouldThrowServiceUnavailableExceptionOnPaymentFallback() {
            // given
            LoopersPgFeginClient.LoopersPaymentRequest request = new LoopersPgFeginClient.LoopersPaymentRequest(
                100L, CardType.SAMSUNG, CardNo.valueOfName("1234567890123456"),
                "http://callback.url", BigDecimal.valueOf(10000), "test-user-123"
            );
            Exception cause = new RuntimeException("Circuit breaker open");

            // when & then
            assertThatThrownBy(() -> processor.paymentFallback(request, cause))
                .isInstanceOf(LoopersPgProcessor.PgServiceTemporarilyUnavailableException.class)
                .hasMessageContaining("PG 서비스가 일시적으로 이용 불가능합니다");
        }

        @Test
        @DisplayName("상태 조회 폴백 시 PgServiceTemporarilyUnavailableException을 던진다")
        void shouldThrowServiceUnavailableExceptionOnStatusCheckFallback() {
            // given
            String transactionKey = "txn-123";
            Exception cause = new RuntimeException("Timeout");

            // when & then
            assertThatThrownBy(() -> processor.getByTransactionKeyFallback(transactionKey, cause))
                .isInstanceOf(LoopersPgProcessor.PgServiceTemporarilyUnavailableException.class)
                .hasMessageContaining("PG 서비스가 일시적으로 이용 불가능하여 결제 상태를 조회할 수 없습니다");
        }
    }

    // 테스트 헬퍼 메서드들
    private LoopersPgFeginClient.PgTransactionResponse createPgTransactionResponse(
            String transactionKey, String status, String reason) {
        LoopersPgFeginClient.PgTransactionResponse response = 
            new LoopersPgFeginClient.PgTransactionResponse();
        response.setTransactionKey(transactionKey);
        response.setStatus(status);
        response.setReason(reason);
        return response;
    }

    private LoopersPgFeginClient.PgTransactionDetailResponse createPgTransactionDetailResponse(
            String transactionKey, String orderId, String status, String reason, Long amount) {
        LoopersPgFeginClient.PgTransactionDetailResponse response = 
            new LoopersPgFeginClient.PgTransactionDetailResponse();
        response.setTransactionKey(transactionKey);
        response.setOrderId(orderId);
        response.setStatus(status);
        response.setReason(reason);
        response.setAmount(amount);
        response.setCardType("SAMSUNG");
        response.setCardNo("1234-****-****-5678");
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }

    private PgResponse<LoopersPgFeginClient.PgTransactionResponse> createSuccessPgResponse(
            LoopersPgFeginClient.PgTransactionResponse data) {
        PgResponse<LoopersPgFeginClient.PgTransactionResponse> response = 
            new PgResponse<>();
        
        // Meta 객체를 먼저 생성하고 설정
        PgResponse.Meta meta = new PgResponse.Meta();
        meta.setResult(PgResponse.Meta.Result.SUCCESS);
        response.setMeta(meta);
        response.setData(data);
        
        return response;
    }

    private PgResponse<LoopersPgFeginClient.PgTransactionResponse> createFailPgResponse(
            String errorCode, String message) {
        PgResponse<LoopersPgFeginClient.PgTransactionResponse> response = 
            new PgResponse<>();
        
        // Meta 객체를 먼저 생성하고 설정
        PgResponse.Meta meta = new PgResponse.Meta();
        meta.setResult(PgResponse.Meta.Result.FAIL);
        meta.setErrorCode(errorCode);
        meta.setMessage(message);
        response.setMeta(meta);
        
        return response;
    }

    private PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> createSuccessDetailPgResponse(
            LoopersPgFeginClient.PgTransactionDetailResponse data) {
        PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> response = 
            new PgResponse<>();
        
        // Meta 객체를 먼저 생성하고 설정
        PgResponse.Meta meta = new PgResponse.Meta();
        meta.setResult(PgResponse.Meta.Result.SUCCESS);
        response.setMeta(meta);
        response.setData(data);
        
        return response;
    }

    private PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> createFailDetailPgResponse(
            String errorCode, String message) {
        PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> response = 
            new PgResponse<>();
        
        // Meta 객체를 먼저 생성하고 설정
        PgResponse.Meta meta = new PgResponse.Meta();
        meta.setResult(PgResponse.Meta.Result.FAIL);
        meta.setErrorCode(errorCode);
        meta.setMessage(message);
        response.setMeta(meta);
        
        return response;
    }
}
