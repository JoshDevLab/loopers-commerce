package com.loopers.infrastructure.payment.pg.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PgResponse<T> {
    private Meta meta;
    private T data;

    // 성공 여부 확인 메서드 추가
    public boolean isSuccess() {
        return meta != null && "SUCCESS".equals(meta.getResult());
    }

    // 실패 여부 확인 메서드 추가  
    public boolean isFailure() {
        return !isSuccess();
    }

    // result 값을 직접 가져오는 편의 메서드
    public String getResult() {
        return meta != null ? meta.getResult() : null;
    }

    // errorCode 값을 직접 가져오는 편의 메서드
    public String getErrorCode() {
        return meta != null ? meta.getErrorCode() : null;
    }

    // message 값을 직접 가져오는 편의 메서드
    public String getMessage() {
        return meta != null ? meta.getMessage() : null;
    }

    // 에러 정보를 포함한 전체 메시지
    public String getFullErrorMessage() {
        if (meta == null) {
            return "응답 메타 정보 없음";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Result: ").append(meta.getResult());
        
        if (meta.getErrorCode() != null) {
            sb.append(", ErrorCode: ").append(meta.getErrorCode());
        }
        
        if (meta.getMessage() != null) {
            sb.append(", Message: ").append(meta.getMessage());
        }
        
        return sb.toString();
    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private String result;
        private String errorCode;
        private String message;
    }
}
