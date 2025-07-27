package com.loopers.domain.product;

public enum ProductStatus {
    ON_SALE,          // 판매 중
    SOLD_OUT,         // 재고 없음 (판매 불가)
    END_OF_SALE       // 판매 종료 (관리자가 판매 중지)
}
