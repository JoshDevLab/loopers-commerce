package com.loopers.interfaces.api.user.dto;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserRegisterCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record SignUpRequest(
        String userId,
        String email,
        String birthday,
        String gender
) {

    public UserRegisterCommand toCommand() {
        try {
            Gender convertGender = Gender.valueOf(gender.toUpperCase());

            return new UserRegisterCommand(
                    userId,
                    email,
                    birthday,
                    convertGender
            );
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.USER_GENDER_ERROR, gender + " 는" + "MALE, FEMALE 만 가능합니다.");
        }
    }
}
