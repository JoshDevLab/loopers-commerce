package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.*;
import com.loopers.infrastructure.payment.LoopersPgFeginClient.PgTransactionData;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    public ExternalPaymentResponse payment(ExternalPaymentRequest paymentRequest) {
        if (!(paymentRequest instanceof LoopersPgFeginClient.LoopersPaymentRequest loopersPaymentRequest)) {
            throw new CoreException(ErrorType.INVALID_PAYMENT_REQUEST_TYPE, "결제 벤더사에 맞지 않는 요청타입 입니다.");
        }

        log.info("Sending payment request to PG - OrderId: {}, Amount: {}", 
                loopersPaymentRequest.getOrderId(), loopersPaymentRequest.getAmount());

        PgResponse<PgTransactionData> pgResponse = client.processPayment(
                loopersPaymentRequest.getUserId(),
                loopersPaymentRequest.toPgRequest()
        );

        // PG 응답 분기 처리
        if (pgResponse.isSuccess()) {
            PgTransactionData data = pgResponse.getData();
            log.info("PG payment response received - TransactionKey: {}, Status: {}", 
                    data.getTransactionKey(), data.getStatus());
            
            return new LoopersPgFeginClient.LoopersPaymentResponse(
                    pgResponse.getResult(),
                    data.getTransactionKey(), 
                    data.getStatus()
            );
        } else {
            // PG 응답 실패 시 Circuit Breaker가 감지할 수 있도록 예외 던지기
            String errorMessage = buildErrorMessage(pgResponse);
            log.error("PG payment failed - {}", errorMessage);
            
            // PG 시뮬레이터의 40% 실패를 Circuit Breaker가 감지하도록 RuntimeException 던지기
            if ("Internal Server Error".equals(pgResponse.getErrorCode())) {
                // PG 서버 불안정 상황 - Circuit Breaker 트리거
                throw new PgServiceUnavailableException("PG 서버 불안정: " + pgResponse.getMessage());
            } else {
                // 기타 PG 오류 - Circuit Breaker 트리거
                throw new PgProcessingException("PG 처리 실패: " + errorMessage);
            }
        }
    }

    @Override
    public ExternalPaymentResponse getByTransactionKey(String transactionId) {
        PgResponse<LoopersPgFeginClient.PgPaymentStatusDetailData> response =
                client.getPaymentStatus(userId, transactionId);

        if (response.isSuccess()) {
            var data = response.getData();
            log.info("Payment status retrieved - TransactionKey: {}, Status: {}",
                    data.getTransactionKey(), data.getStatus());

            return new LoopersPgFeginClient.LoopersPaymentDetailResponse(
                    response.getResult(),
                    data.getTransactionKey(),
                    data.getStatus(),
                    data.getReason(),
                    null, // errorCode는 성공 시 null
                    null  // message는 성공 시 null
            );
        } else {
            // PG 응답 실패 시 Circuit Breaker가 감지할 수 있도록 예외 던지기
            String errorMessage = buildErrorMessage(response);
            log.error("Payment status check failed - {}", errorMessage);
            throw new PgProcessingException("결제 상태 조회 실패: " + errorMessage);
        }
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
    public static class PgServiceUnavailableException extends RuntimeException {
        public PgServiceUnavailableException(String message) {
            super(message);
        }
    }
    
    public static class PgProcessingException extends RuntimeException {
        public PgProcessingException(String message) {
            super(message);
        }
    }
}
