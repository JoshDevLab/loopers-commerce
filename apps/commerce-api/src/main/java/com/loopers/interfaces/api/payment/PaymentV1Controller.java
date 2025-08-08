package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentV1Controller {
    private final PaymentFacade paymentFacade;

    @PostMapping
    public ApiResponse<PaymentInfo> payment(@RequestBody PaymentRequest paymentRequest) {
        return ApiResponse.success(paymentFacade.payment(paymentRequest.toCommand()));
    }
}
