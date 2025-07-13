package com.loopers.interfaces.api.user.dto;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;

import java.time.LocalDate;

public record SignUpResponse(
        String userId,
        String email,
        LocalDate birthday,
        Gender gender
) {
    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getUserId(),
                user.getEmail(),
                user.getBirthDay(),
                user.getGender()
        );
    }
}
