package com.loopers.infrastructure.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.ExternalPaymentRequest;
import com.loopers.domain.payment.ExternalPaymentResponse;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import com.loopers.infrastructure.payment.pg.config.PgSimulatorFeignConfig;
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
    PgResponse<PgTransactionData> processPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgPaymentRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgResponse<PgPaymentStatusDetailData> getPaymentStatus(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable String transactionKey);

    @GetMapping("/api/v1/payments")
    PgResponse<List<PgPaymentStatusDetailData>> getPaymentsByOrderId(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String orderId);

    record PgPaymentRequest(
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {}

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PgTransactionData {
        private String transactionKey;
        private String status;

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

    @Getter
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PgPaymentStatusDetailData {
        private final String transactionKey;
        private final String orderId;
        private final String cardType;
        private final String cardNo;
        private final Long amount;
        private final String status;
        private final String reason;
        private final LocalDateTime processedAt;
    }

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
            return new PgPaymentRequest(
                    orderId.toString(),
                    cardType.name(),
                    cardNo.getValue(),
                    super.getAmount().longValue(),
                    callbackUrl
            );
        }

    }

    @Getter
    @AllArgsConstructor
    class LoopersPaymentResponse extends ExternalPaymentResponse {
        private final String result;
        private final String transactionKey;
        private final String status;

        @Override
        public String getTransactionId() {
            return transactionKey;
        }
    }

    @Getter
    @AllArgsConstructor
    class LoopersPaymentDetailResponse extends ExternalPaymentResponse {
        private final String result;
        private final String transactionKey;
        private final String status;
        private final String reason;
        private final String errorCode;
        private final String message;

        @Override
        public String getTransactionId() {
            return transactionKey;
        }

        public boolean isSuccess() {
            return "SUCCESS".equals(result);
        }

        public String getFullErrorInfo() {
            if (!hasError()) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            if (errorCode != null) {
                sb.append("ErrorCode: ").append(errorCode);
            }
            if (message != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append("Message: ").append(message);
            }
            return sb.toString();
        }

        private boolean hasError() {
            return "FAIL".equals(result);
        }
    }
}
