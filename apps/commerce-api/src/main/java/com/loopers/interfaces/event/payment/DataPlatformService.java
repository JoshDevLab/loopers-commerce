package com.loopers.interfaces.event.payment;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.domain.payment.DataPlatformGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DataPlatformService {

    private final DataPlatformGateway dataPlatformGateway;

    @Transactional(readOnly = true)
    public void send(DataPlatformRequest request) {
        dataPlatformGateway.send(request);
    }

    public record DataPlatformRequest(
            OrderInfo orderInfo,
            List<PaymentInfo> paymentInfo
    ) {
    }
}
