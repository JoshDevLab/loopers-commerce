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

    /* User 도메인 에러 */
    USER_ID_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "유저 아이디는 영문 및 숫자 10자 이내이어야 합니다."),
    USER_EMAIL_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "이메일이 xx@yy.zz 형식에 맞지 않습니다."),
    USER_BIRTHDAY_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "생년월일이 yyyy-MM-dd 형식에 맞지 않습니다."),
    ALREADY_EXIST_USERID(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "이미 동일한 아이디가 존재합니다."),
    USER_GENDER_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "유효하지 않은 성별입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 유저 입니다."),
    POINT_CHARGING_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "0 이하의 정수로 포인트를 충전 시 실패"),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "포인트가 존재하지 않습니다."),
    POINT_USING_ERROR(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "포인트 사용이 실패하였습니다."),
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
    INVALID_PAID_AMOUNT(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "총 결제 금액은 0원 이상이어야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
