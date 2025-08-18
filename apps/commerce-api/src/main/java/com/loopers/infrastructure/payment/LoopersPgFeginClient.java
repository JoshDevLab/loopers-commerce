package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.ExternalPaymentRequest;
import com.loopers.infrastructure.payment.pg.config.PgSimulatorFeignConfig;
import com.loopers.interfaces.api.payment.dto.CardNo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "loopers-pg", url = "${loopers.pg.url}", configuration = PgSimulatorFeignConfig.class)
public interface LoopersPgFeginClient {
    
    @PostMapping("/payment")
    LoopersPaymentResponse processPayment(@RequestBody LoopersPaymentRequest request);

    class LoopersPaymentRequest extends ExternalPaymentRequest {
        private Long orderId;
        private CardType cardType;
        private CardNo cardNo;
        private String callbackUrl;

        public LoopersPaymentRequest(Long orderId,
                                     CardType cardType,
                                     CardNo cardNo,
                                     String callbackUrl,
                                     BigDecimal amount) {
            super(amount);
            this.orderId = orderId;
            this.cardType = cardType;
            this.cardNo = cardNo;
            this.callbackUrl = callbackUrl;
        }
    }

    class LoopersPaymentResponse extends ExternalPaymentResponse {
        private Long orderId;
        private CardType cardType;
        private CardNo cardNo;
        private String callbackUrl;
        private BigDecimal amount;

        public LoopersPaymentResponse(Long orderId,
                                     CardType cardType,
                                     CardNo cardNo,
                                     String callbackUrl,
                                     BigDecimal amount) {
            this.orderId = orderId;
            this.cardType = cardType;
            this.cardNo = cardNo;
            this.callbackUrl = callbackUrl;
            this.amount = amount;
        }
    }
}
