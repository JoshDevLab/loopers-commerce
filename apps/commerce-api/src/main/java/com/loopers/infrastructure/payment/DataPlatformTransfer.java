package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.DataPlatformGateway;
import com.loopers.interfaces.event.payment.DataPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataPlatformTransfer implements DataPlatformGateway {
    @Override
    public void send(DataPlatformService.DataPlatformRequest request) {
        log.info("[데이터 플랫폼 전송] request = {}", request);

    }
}
