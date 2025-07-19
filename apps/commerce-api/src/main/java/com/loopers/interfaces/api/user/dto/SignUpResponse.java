package com.loopers.interfaces.api.user.dto;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;

import java.time.LocalDate;

public record SignUpResponse(
        String userId,
        String email,
        LocalDate birthday,
        Gender gender
) {
    public static SignUpResponse from(UserInfo user) {
        return new SignUpResponse(
                user.userId(),
                user.email(),
                user.birthday(),
                user.gender()
        );
    }
}
