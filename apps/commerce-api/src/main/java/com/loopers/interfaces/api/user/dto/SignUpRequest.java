package com.loopers.interfaces.api.user.dto;

import com.loopers.domain.user.UserCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record SignUpRequest(
        String userId,
        String email,
        String birthday,
        String gender
) {

    public UserCommand.Register toCommand() {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.USER_ID_ERROR, "유저 아이디는 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new CoreException(ErrorType.USER_EMAIL_ERROR, "이메일은 필수입니다.");
        }
        if (birthday == null || birthday.isBlank()) {
            throw new CoreException(ErrorType.USER_BIRTHDAY_ERROR, "생일은 필수입니다.");
        }
        if (gender == null || gender.isBlank()) {
            throw new CoreException(ErrorType.USER_GENDER_ERROR, "성별은 필수입니다.");
        }
        return new UserCommand.Register(
                userId,
                email,
                birthday,
                gender
        );
    }
}
