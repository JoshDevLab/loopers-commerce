package com.loopers.domain.user;

import java.time.LocalDate;

public record UserInfo(
        String userId,
        String email,
        LocalDate birthday,
        Gender gender
) {

    public static UserInfo of(User user) {
        return new UserInfo(
                user.getUserId(),
                user.getEmail(),
                user.getBirthDay(),
                user.getGender()
        );
    }
}
