package com.loopers.infrastructure.payment.pg.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.infrastructure.payment.pg.support.PgResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class PgErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    // PG 시뮬레이터의 에러 메시지 패턴들
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("카드 번호.*형식");
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("주문 ID.*문자열");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("결제금액.*정수");
    private static final Pattern CALLBACK_URL_PATTERN = Pattern.compile("콜백 URL");

    @Override
    public Exception decode(String methodKey, Response response) {
        String logMessage = String.format("PG API 호출 실패 - Method: %s, Status: %d", 
                methodKey, response.status());
        
        log.error("{} - Headers: {}", logMessage, response.headers());
        
        // 응답 본문에서 PG 에러 정보 추출
        PgErrorInfo errorInfo = extractErrorInfo(response);
        log.debug("PG Error Info - ErrorCode: {}, Message: {}", 
                errorInfo.errorCode(), errorInfo.message());
        
        return switch (response.status()) {
            case 400 -> handleBadRequestError(errorInfo, logMessage);
            case 500 -> handleInternalServerError(errorInfo, logMessage);
            case 503 -> new PgServiceUnavailableException(logMessage + " - PG 서비스 일시 이용 불가");
            case 504 -> new PgTimeoutException(logMessage + " - PG 응답 시간 초과");
            default -> new PgGeneralException(logMessage + " - 알 수 없는 PG 오류 (status: " + response.status() + ")");
        };
    }
    
    /**
     * 400 Bad Request 에러 처리
     * PG 시뮬레이터의 검증 오류를 구체적인 예외로 변환
     */
    private Exception handleBadRequestError(PgErrorInfo errorInfo, String logMessage) {
        String message = errorInfo.message();
        
        // PG 시뮬레이터의 구체적인 검증 오류 메시지 분석
        if (message != null) {
            if (CARD_NUMBER_PATTERN.matcher(message).find()) {
                log.warn("카드번호 형식 오류 - Message: {}", message);
                return new PgValidationException("cardNo", message);
            }
            
            if (ORDER_ID_PATTERN.matcher(message).find()) {
                log.warn("주문ID 형식 오류 - Message: {}", message);
                return new PgValidationException("orderId", message);
            }
            
            if (AMOUNT_PATTERN.matcher(message).find()) {
                log.warn("결제금액 오류 - Message: {}", message);
                return new PgValidationException("amount", message);
            }
            
            if (CALLBACK_URL_PATTERN.matcher(message).find()) {
                log.warn("콜백URL 형식 오류 - Message: {}", message);
                return new PgValidationException("callbackUrl", message);
            }
        }
        
        // 기타 400 에러는 일반적인 요청 오류로 처리
        return new PgBadRequestException(
                errorInfo.errorCode(), 
                errorInfo.message() != null ? errorInfo.message() : "요청 형식이 올바르지 않습니다."
        );
    }
    
    /**
     * 500 Internal Server Error 처리
     * PG 시뮬레이터의 서버 불안정 상황
     */
    private PgServerErrorException handleInternalServerError(PgErrorInfo errorInfo, String logMessage) {
        log.error("PG 서버 불안정 - ErrorCode: {}, Message: {}", 
                errorInfo.errorCode(), errorInfo.message());
        
        // PG 시뮬레이터의 40% 실패 로직에 의한 서버 오류
        if (errorInfo.message() != null && errorInfo.message().contains("서버가 불안정")) {
            return new PgServerErrorException(
                    "PG_SERVER_UNSTABLE", 
                    errorInfo.message()
            );
        }
        
        // 기타 서버 오류
        return new PgServerErrorException(
                errorInfo.errorCode() != null ? errorInfo.errorCode() : "INTERNAL_SERVER_ERROR",
                errorInfo.message() != null ? errorInfo.message() : "PG 서버 내부 오류가 발생했습니다."
        );
    }
    
    /**
     * PG 응답에서 에러 정보 추출
     */
    private PgErrorInfo extractErrorInfo(Response response) {
        try {
            if (response.body() != null) {
                String body = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
                log.debug("PG Error Response Body: {}", body);
                
                // JSON 응답 파싱하여 meta 정보 추출
                PgResponse<?> pgResponse = objectMapper.readValue(body, PgResponse.class);
                
                if (pgResponse.getMeta() != null) {
                    return new PgErrorInfo(
                            pgResponse.getErrorCode(),
                            pgResponse.getMessage()
                    );
                }
            }
        } catch (IOException e) {
            log.warn("PG 응답 본문 파싱 실패", e);
        }
        
        return new PgErrorInfo("UNKNOWN_ERROR", "알 수 없는 오류가 발생했습니다.");
    }
    
    /**
     * PG 에러 정보를 담는 레코드
     */
    private record PgErrorInfo(String errorCode, String message) {}
}
