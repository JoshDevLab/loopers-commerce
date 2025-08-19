package com.loopers.infrastructure.payment.pg.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PgSimulatorFeignConfig {

    @Value("${app.pg-simulator.user-id:135135}")
    private String defaultUserId;
    
    @Value("${app.pg-simulator.timeout.connect:3000}")
    private int connectTimeout;
    
    @Value("${app.pg-simulator.timeout.read:15000}")
    private int readTimeout;

    @Bean
    public Logger.Level pgSimulatorLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options pgSimulatorOptions() {
        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,   // connect timeout
                readTimeout, TimeUnit.MILLISECONDS,      // read timeout
                true                                     // follow redirects
        );
    }

    @Bean
    public Retryer pgSimulatorRetryer() {
        // PG 결제는 중복 방지를 위해 재시도 하지 않음
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public RequestInterceptor pgSimulatorRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");

            // X-USER-ID 헤더가 없으면 기본값 설정
            if (!requestTemplate.headers().containsKey("X-USER-ID")) {
                requestTemplate.header("X-USER-ID", defaultUserId);
            }
            
            // 요청 추적을 위한 ID 추가
            String requestId = "PG_REQ_" + System.currentTimeMillis();
            requestTemplate.header("X-Request-ID", requestId);
        };
    }

}
