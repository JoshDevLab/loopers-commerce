package com.loopers.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    /**
     * 범용 에러
     */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다."),

    USER_ID_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "유저 아이디는 영문 및 숫자 10자 이내이어야 합니다."),
    USER_EMAIL_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "이메일이 xx@yy.zz 형식에 맞지 않습니다."),
    USER_BIRTHDAY_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "생년월일이 yyyy-MM-dd 형식에 맞지 않습니다."),
    ALREADY_EXIST_USERID(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "이미 동일한 아이디가 존재합니다."),
    USER_GENDER_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "유효하지 않은 성별입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 유저 입니다."),
    POINT_CHARGING_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "0 이하의 정수로 포인트를 충전 시 실패"),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "포인트가 존재하지 않습니다."),
    POINT_USING_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "포인트 사용이 실패하였습니다."),
    POINT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "포인트 이력을 찾을 수 없습니다."),
    INVALID_PRODUCT_CATEGORY(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "존재하지 않는 상품 카테고리입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "해당 상품을 찾을 수 없습니다."),
    PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "해당 상품의 옵션을 찾을 수 없습니다."),
    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "해당 브랜드를 찾을 수 없습니다."),
    PRODUCT_OPTION_NOT_ON_SALE(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "해당 상품은 판매중인 상품이 아닙니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "상품 옵션 재고가 부족합니다."),
    PRODUCT_INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "상품 재고를 찾을 수 없습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "포인트가 부족합니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "주문을 찾을 수 없습니다."),
    ALREADY_USING_COUPON(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "이미 사용된 쿠폰입니다."),
    EXPIRED_COUPON(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "만료된 쿠폰입니다."),
    INVALID_COUPON(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 쿠폰입니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "쿠폰을 찾을 수 없습니다."),
    INVALID_PAID_AMOUNT(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "총 결제 금액은 0원 초과이어야 합니다."),
    UNSUPPORTED_PAYMENT_TYPE(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "지원하지 않는 결제 수단입니다."),
    UNSUPPORTED_CARD_TYPE(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "지원하지 않는 카드사 결제입니다."),
    INVENTORY_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "재고 이력을 찾을 수 없습니다."),
    COUPON_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "쿠폰 이력을 찾을 수 없습니다."),
    ALREADY_EXIST_ORDER_PAYMENT(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "이미 해당 주문의 결제를 완료하였습니다."),
    PAYMENT_FAIL(HttpStatus.BAD_GATEWAY, HttpStatus.BAD_GATEWAY.getReasonPhrase(), "외부 결제 서버 오류입니다."),
    PRODUCT_LIST_CACHING_FAIL(HttpStatus.BAD_GATEWAY, HttpStatus.BAD_GATEWAY.getReasonPhrase(), "상품 목록 캐싱에 실패하였습니다."),
    INVALID_PAYMENT_REQUEST_TYPE(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "결제 벤더사에 맞지 않는 요청타입니다."),
    INVALID_CARD_NO(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 형식의 카드번호 입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "결제 정보를 찾을 수 없습니다."),
    
    // 사용자 친화적 결제 오류 메시지들
    INVALID_CARD_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_CARD_NUMBER", "입력하신 카드번호가 올바르지 않습니다."),
    EXPIRED_CARD(HttpStatus.BAD_REQUEST, "EXPIRED_CARD", "카드 유효기간이 만료되었습니다."),
    INVALID_CARD_EXPIRY(HttpStatus.BAD_REQUEST, "INVALID_CARD_EXPIRY", "카드 유효기간이 올바르지 않습니다."),
    INVALID_CVC(HttpStatus.BAD_REQUEST, "INVALID_CVC", "카드 보안코드가 올바르지 않습니다."),
    CARD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CARD_LIMIT_EXCEEDED", "카드 이용한도를 초과했습니다."),
    DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "DAILY_LIMIT_EXCEEDED", "일일 결제한도를 초과했습니다."),
    MONTHLY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MONTHLY_LIMIT_EXCEEDED", "월 결제한도를 초과했습니다."),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", "잔액이 부족합니다."),
    CARD_BLOCKED(HttpStatus.BAD_REQUEST, "CARD_BLOCKED", "카드가 차단되어 있습니다."),
    PAYMENT_AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTHENTICATION_FAILED", "카드 인증에 실패했습니다."),
    PAYMENT_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_SUPPORTED", "해당 카드로는 결제할 수 없습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_FAILED", "결제 처리 중 오류가 발생했습니다."),
    PAYMENT_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_SYSTEM_ERROR", "결제 시스템에 일시적인 문제가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
