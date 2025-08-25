package com.loopers.interfaces.event.payment;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DataPlatformService {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final DataPlatformTransfer dataPlatformTransfer;

    @Transactional(readOnly = true)
    public void send(Long orderId) {
        Order order = orderService.findById(orderId);
        List<Payment> payments = paymentService.findByOrderId(orderId);
        DataPlatformRequest request = new DataPlatformRequest(OrderInfo.from(order), payments.stream().map(PaymentInfo::of).toList());
        dataPlatformTransfer.send(request);
    }

    public record DataPlatformRequest(
            OrderInfo orderInfo,
            List<PaymentInfo> paymentInfo
    ) {
    }
}
