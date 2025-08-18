package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public enum CardType {
    SAMSUNG,
    HYUNDAI,
    KOOKMIN,
    ;

    public static CardType valueOfName(String cardType) {
        String upperCaseName = cardType.toUpperCase();
        try {
            return CardType.valueOf(upperCaseName);
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.UNSUPPORTED_PAYMENT_TYPE, "지원하지 않는 CardType : " + cardType);
        }
    }
}
