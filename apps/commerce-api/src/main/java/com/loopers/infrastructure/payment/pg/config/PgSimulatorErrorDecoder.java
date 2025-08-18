package com.loopers.infrastructure.payment.pg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class PgSimulatorErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = extractErrorMessage(response);
        
        log.error("PG Simulator error - Method: {}, Status: {}, Message: {}", 
                methodKey, response.status(), errorMessage);

        return switch (response.status()) {
            case 400 -> new PgClientException("PG 요청 오류: " + errorMessage, response.status());
            case 401 -> new PgClientException("PG 인증 실패: " + errorMessage, response.status());
            case 404 -> new PgClientException("PG 리소스 없음: " + errorMessage, response.status());
            case 408 -> new PgClientException("PG 요청 타임아웃: " + errorMessage, response.status());
            case 422 -> new PgClientException("PG 처리 불가: " + errorMessage, response.status());
            case 429 -> new PgClientException("PG 요청 제한: " + errorMessage, response.status());
            case 500 -> new PgServerException("PG 서버 오류: " + errorMessage, response.status());
            case 502 -> new PgServerException("PG 게이트웨이 오류: " + errorMessage, response.status());
            case 503 -> new PgServerException("PG 서비스 불가: " + errorMessage, response.status());
            case 504 -> new PgServerException("PG 게이트웨이 타임아웃: " + errorMessage, response.status());
            default -> new PgClientException("PG 알 수 없는 오류: " + errorMessage, response.status());
        };
    }

    private String extractErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                String body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);

                try {
                    var errorNode = objectMapper.readTree(body);
                    
                    if (errorNode.has("message")) {
                        return errorNode.get("message").asText();
                    }
                    if (errorNode.has("error")) {
                        var errorDetail = errorNode.get("error");
                        if (errorDetail.isTextual()) {
                            return errorDetail.asText();
                        }
                        if (errorDetail.has("message")) {
                            return errorDetail.get("message").asText();
                        }
                    }
                    
                    return body.length() > 200 ? body.substring(0, 200) + "..." : body;
                    
                } catch (Exception e) {
                    return body.length() > 200 ? body.substring(0, 200) + "..." : body;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read PG error response body", e);
        }
        
        return "PG 응답 메시지 없음";
    }

    @Getter
    public static class PgClientException extends RuntimeException {
        private final int status;

        public PgClientException(String message, int status) {
            super(message);
            this.status = status;
        }

    }

    @Getter
    public static class PgServerException extends RuntimeException {
        private final int status;

        public PgServerException(String message, int status) {
            super(message);
            this.status = status;
        }

    }
}
