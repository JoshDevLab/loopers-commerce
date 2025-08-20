package com.loopers.infrastructure.payment.pg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.ExternalPaymentRequest;
import com.loopers.domain.payment.ExternalPaymentResponse;
import com.loopers.infrastructure.payment.pg.config.PgSimulatorFeignConfig;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import com.loopers.interfaces.api.payment.dto.CardNo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "loopers-pg", url = "${app.pg-simulator.base-url}", configuration = PgSimulatorFeignConfig.class)
public interface LoopersPgFeginClient {
    
    @PostMapping("/api/v1/payments")
    PgResponse<PgTransactionResponse> processPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgPaymentRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgResponse<PgTransactionDetailResponse> getPaymentStatus(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable String transactionKey);

    @GetMapping("/api/v1/payments")
    PgResponse<PgOrderResponse> getPaymentsByOrderId(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String orderId);

    
    record PgPaymentRequest(
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {}
    
    /**
     * 결제 요청 응답 (POST /api/v1/payments)
     */
    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PgTransactionResponse {
        private String transactionKey;
        private String status; // PENDING, SUCCESS, FAILED
        private String reason;

        public boolean isPending() {
            return "PENDING".equals(status);
        }

        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }

        public boolean isFailed() {
            return "FAILED".equals(status);
        }
    }
    
    /**
     * 결제 상세 조회 응답 (GET /api/v1/payments/{transactionKey})
     */
    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PgTransactionDetailResponse {
        private String transactionKey;
        private String orderId;
        private String cardType;
        private String cardNo;
        private Long amount;
        private String status;
        private String reason;
        private LocalDateTime processedAt;

        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }

        public boolean isFailed() {
            return "FAILED".equals(status);
        }
    }
    
    /**
     * 주문별 결제 조회 응답 (GET /api/v1/payments?orderId=xxx)
     */
    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PgOrderResponse {
        private String orderId;
        private List<PgTransactionResponse> transactions;
    }

    // ==================== 래퍼 클래스들 ====================

    @Getter
    class LoopersPaymentRequest extends ExternalPaymentRequest {
        private final Long orderId;
        private final CardType cardType;
        private final CardNo cardNo;
        private final String callbackUrl;
        private final String userId;

        public LoopersPaymentRequest(Long orderId,
                                     CardType cardType,
                                     CardNo cardNo,
                                     String callbackUrl,
                                     BigDecimal amount,
                                     String userId) {
            super(amount);
            this.orderId = orderId;
            this.cardType = cardType;
            this.cardNo = cardNo;
            this.callbackUrl = callbackUrl;
            this.userId = userId;
        }

        public PgPaymentRequest toPgRequest() {
            // PG 시뮬레이터가 요구하는 카드번호 형식: xxxx-xxxx-xxxx-xxxx
            String formattedCardNo = formatCardNumber(cardNo.getValue());
            
            return new PgPaymentRequest(
                    orderId.toString(),
                    cardType.name(),
                    formattedCardNo,
                    super.getAmount().longValue(),
                    callbackUrl
            );
        }
        
        private String formatCardNumber(String cardNo) {
            // 16자리 카드번호를 xxxx-xxxx-xxxx-xxxx 형식으로 변환
            if (cardNo.length() == 16) {
                return String.format("%s-%s-%s-%s",
                        cardNo.substring(0, 4),
                        cardNo.substring(4, 8),
                        cardNo.substring(8, 12),
                        cardNo.substring(12, 16));
            }
            return cardNo; // 이미 형식이 맞는 경우 그대로 반환
        }
    }

    @Getter
    @AllArgsConstructor
    class LoopersPaymentResponse extends ExternalPaymentResponse {
        private final String result;
        private final String transactionKey;
        private final String status;
        private final String reason;

        @Override
        public String getTransactionId() {
            return transactionKey;
        }
        
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
        
        public boolean isFailed() {
            return "FAILED".equals(status);
        }
        
        public boolean isPending() {
            return "PENDING".equals(status);
        }
    }

    @Getter
    @AllArgsConstructor
    class LoopersPaymentDetailResponse extends ExternalPaymentResponse {
        private final String result;
        private final String transactionKey;
        private final String orderId;
        private final String status;
        private final String reason;
        private final String cardType;
        private final String cardNo;
        private final Long amount;
        private final LocalDateTime processedAt;

        @Override
        public String getTransactionId() {
            return transactionKey;
        }

        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }

        public boolean isFailed() {
            return "FAILED".equals(status);
        }
        
        public boolean isPending() {
            return "PENDING".equals(status);
        }
        
        // PG 시뮬레이터의 실패 사유를 반환
        public String getFailureReason() {
            return reason;
        }
    }
}
