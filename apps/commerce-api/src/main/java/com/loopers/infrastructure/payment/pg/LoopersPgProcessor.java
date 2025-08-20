package com.loopers.infrastructure.payment.pg;

import com.loopers.domain.payment.*;
import com.loopers.infrastructure.payment.pg.exception.PgBusinessException;
import com.loopers.infrastructure.payment.pg.exception.PgException;
import com.loopers.infrastructure.payment.pg.exception.PgGeneralException;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoopersPgProcessor implements PaymentProcessor {
    private final LoopersPgFeginClient client;

    @Value("${app.pg-simulator.user-id:135135}")
    private String userId;

    @Override
    public Payment.PaymentType getPaymentType() {
        return Payment.PaymentType.CARD;
    }

    @Override
    public ExternalPaymentRequest createRequest(PaymentCommand.Request paymentCommand, BigDecimal paidAmount) {
        return new LoopersPgFeginClient.LoopersPaymentRequest(
                paymentCommand.orderId(),
                paymentCommand.cardType(),
                paymentCommand.cardNo(),
                paymentCommand.callbackUrl(),
                paidAmount,
                userId
        );
    }

    @Override
    @CircuitBreaker(name = "pgPayment", fallbackMethod = "paymentFallback")
    public ExternalPaymentResponse payment(ExternalPaymentRequest paymentRequest) {
        if (!(paymentRequest instanceof LoopersPgFeginClient.LoopersPaymentRequest loopersPaymentRequest)) {
            throw new CoreException(ErrorType.INVALID_PAYMENT_REQUEST_TYPE, "결제 벤더사에 맞지 않는 요청타입 입니다.");
        }

        log.info("Sending payment request to PG - OrderId: {}, Amount: {}", 
                loopersPaymentRequest.getOrderId(), loopersPaymentRequest.getAmount());

        try {
            PgResponse<LoopersPgFeginClient.PgTransactionResponse> pgResponse = client.processPayment(
                    loopersPaymentRequest.getUserId(),
                    loopersPaymentRequest.toPgRequest()
            );

            // PG API 응답 처리
            if (pgResponse.isSuccess() && pgResponse.getData() != null) {
                LoopersPgFeginClient.PgTransactionResponse data = pgResponse.getData();
                log.info("PG payment response received - TransactionKey: {}, Status: {}", 
                        data.getTransactionKey(), data.getStatus());
                
                // PG 시뮬레이터의 비즈니스 로직 실패 확인 (HTTP 200이지만 status가 FAILED)
                if (data.isFailed()) {
                    log.warn("PG 비즈니스 로직 실패 - TransactionKey: {}, Reason: {}", 
                            data.getTransactionKey(), data.getReason());
                    throw new PgBusinessException(
                            data.getTransactionKey(),
                            data.getStatus(),
                            data.getReason()
                    );
                }
                
                return new LoopersPgFeginClient.LoopersPaymentResponse(
                        pgResponse.getResult(),
                        data.getTransactionKey(), 
                        data.getStatus(),
                        data.getReason()
                );
            } else {
                // PG API에서 실패 응답을 받은 경우 (meta.result가 FAIL)
                String errorMessage = buildErrorMessage(pgResponse);
                log.error("PG payment API failed - {}", errorMessage);
                
                throw new PgGeneralException("PG API 응답 실패: " + errorMessage);
            }
        } catch (PgException e) {
            // ErrorDecoder에서 변환된 PG 예외를 다시 던져서 Circuit Breaker가 처리하도록 함
            log.error("PG exception occurred during payment processing", e);
            throw e;
        } catch (Exception e) {
            // 기타 예외는 일반적인 PG 처리 오류로 간주
            log.error("Unexpected error during payment processing", e);
            throw new PgProcessingException("결제 처리 중 예상치 못한 오류 발생", e);
        }
    }

    @Override
    @CircuitBreaker(name = "pgPayment", fallbackMethod = "getByTransactionKeyFallback")
    public ExternalPaymentResponse getByTransactionKey(String transactionId) {
        log.info("Requesting payment status from PG - TransactionKey: {}", transactionId);
        
        try {
            PgResponse<LoopersPgFeginClient.PgTransactionDetailResponse> response =
                    client.getPaymentStatus(userId, transactionId);

            if (response.isSuccess() && response.getData() != null && !response.getData().isFailed()) {
                var data = response.getData();
                log.info("Payment status retrieved - TransactionKey: {}, Status: {}",
                        data.getTransactionKey(), data.getStatus());

                return new LoopersPgFeginClient.LoopersPaymentDetailResponse(
                        response.getResult(),
                        data.getTransactionKey(),
                        data.getOrderId(),
                        data.getStatus(),
                        data.getReason(),
                        data.getCardType(),
                        data.getCardNo(),
                        data.getAmount(),
                        data.getProcessedAt()
                );
            } else {
                String errorMessage = buildErrorMessage(response);
                log.error("Payment status check failed - {}", errorMessage);
                throw new PgGeneralException(errorMessage);
            }
        } catch (PgException e) {
            // ErrorDecoder에서 변환된 PG 예외를 다시 던져서 Circuit Breaker가 처리하도록 함
            log.error("PG exception occurred during status check", e);
            throw e;
        } catch (Exception e) {
            // 기타 예외는 일반적인 PG 처리 오류로 간주
            log.error("Unexpected error during status check", e);
            throw new PgProcessingException("결제 상태 조회 중 예상치 못한 오류 발생", e);
        }
    }

    /**
     * 결제 요청 실패 시 폴백 메서드
     * Circuit Breaker가 OPEN 상태이거나 타임아웃 발생 시 호출됨
     */
    public ExternalPaymentResponse paymentFallback(ExternalPaymentRequest paymentRequest, Exception ex) {
        log.error("Payment fallback triggered for orderId: {}, reason: {}", 
                ((LoopersPgFeginClient.LoopersPaymentRequest) paymentRequest).getOrderId(), 
                ex.getMessage());
        
        // 폴백 시에는 실패 응답을 반환하고, 상위 레이어에서 적절한 처리를 하도록 함
        throw new PgServiceTemporarilyUnavailableException(
                "PG 서비스가 일시적으로 이용 불가능합니다. 잠시 후 다시 시도해주세요.", ex);
    }

    /**
     * 결제 상태 조회 실패 시 폴백 메서드
     */
    public ExternalPaymentResponse getByTransactionKeyFallback(String transactionId, Exception ex) {
        log.error("Payment status check fallback triggered for transactionId: {}, reason: {}", 
                transactionId, ex.getMessage());
        
        // 폴백 시에는 상태 조회 불가 응답을 반환
        throw new PgServiceTemporarilyUnavailableException(
                "PG 서비스가 일시적으로 이용 불가능하여 결제 상태를 조회할 수 없습니다.", ex);
    }

    // PG 에러 메시지 생성 헬퍼 메서드
    private String buildErrorMessage(PgResponse<?> pgResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Result: ").append(pgResponse.getResult());
        
        if (pgResponse.getErrorCode() != null) {
            sb.append(", ErrorCode: ").append(pgResponse.getErrorCode());
        }
        
        if (pgResponse.getMessage() != null) {
            sb.append(", Message: ").append(pgResponse.getMessage());
        }
        
        return sb.toString();
    }
    
    // Circuit Breaker가 감지할 PG 전용 예외들
    public static class PgProcessingException extends PgException {
        public PgProcessingException(String message) {
            super(message);
        }
        
        public PgProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
        
        @Override
        public boolean isRetryable() {
            return false; // 결제는 재시도하지 않음
        }
        
        @Override
        public boolean shouldRecordAsFailure() {
            return true; // Circuit Breaker에 실패로 기록
        }
    }
    
    public static class PgServiceTemporarilyUnavailableException extends PgException {
        public PgServiceTemporarilyUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
        
        @Override
        public boolean isRetryable() {
            return false; // 결제는 재시도하지 않음
        }
        
        @Override
        public boolean shouldRecordAsFailure() {
            return true; // Circuit Breaker에 실패로 기록
        }
    }
}
