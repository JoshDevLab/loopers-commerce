package com.loopers.interfaces.api.payment.dto;

import com.loopers.domain.payment.PaymentCommand;
import lombok.Getter;
import lombok.ToString;

/**
 * PG 시뮬레이터에서 전송하는 콜백 데이터
 */
@Getter
@ToString
public class PaymentCallbackRequest {
    private String transactionKey;
    private String orderId;
    private String cardType;
    private String cardNo;
    private Long amount;
    private String status;
    private String reason;

    public PaymentCommand.CallbackRequest toCommand() {
        return new PaymentCommand.CallbackRequest(
                transactionKey,
                orderId,
                cardType,
                cardNo,
                amount,
                status,
                reason
        );
    }
}
