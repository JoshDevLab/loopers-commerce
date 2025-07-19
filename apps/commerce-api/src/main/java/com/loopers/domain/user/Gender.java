package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public enum Gender {
    MALE,
    FEMALE;

    public static Gender validate(String gender) {
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.USER_GENDER_ERROR, gender + " 는" + "MALE, FEMALE 만 가능합니다.");
        }
    }
}
