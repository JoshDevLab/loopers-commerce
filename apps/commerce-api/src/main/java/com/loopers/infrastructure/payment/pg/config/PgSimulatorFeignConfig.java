package com.loopers.infrastructure.payment.pg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.infrastructure.payment.pg.exception.PgErrorDecoder;
import feign.Logger;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PgSimulatorFeignConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * PG 시뮬레이터 전용 ErrorDecoder
     * HTTP 에러 응답을 적절한 PG 예외로 변환
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new PgErrorDecoder(objectMapper);
    }
}
