package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    SHOES,
    CLOTHING,
    BAG,
    ACCESSORY;

    public static ProductCategory valueOfName(String name) {
        String upperCaseName = name.toUpperCase();
        try {
            return ProductCategory.valueOf(upperCaseName);
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.INVALID_PRODUCT_CATEGORY, "존재하지 않는 카테고리입니다: " + name);
        }
    }
}
