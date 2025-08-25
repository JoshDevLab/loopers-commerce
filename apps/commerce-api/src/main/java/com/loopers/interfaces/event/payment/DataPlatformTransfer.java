package com.loopers.interfaces.event.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataPlatformTransfer {
    void send(DataPlatformService.DataPlatformRequest request) {
        log.info("[데이터 플랫폼 전송] request = {}", request);
    }
}
