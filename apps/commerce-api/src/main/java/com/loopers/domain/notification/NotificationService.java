package com.loopers.domain.notification;

import com.loopers.domain.payment.PaymentCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    public void sendPaymentSyncFailureAlert(PaymentCommand.CallbackRequest command) {
        log.info("Sending payment sync failure alert for payment command {}", command);
    }
}
