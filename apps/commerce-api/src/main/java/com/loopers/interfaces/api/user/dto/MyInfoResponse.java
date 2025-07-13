package com.loopers.interfaces.api.user.dto;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;

import java.time.LocalDate;

public record MyInfoResponse(
        String userId,
        String email,
        LocalDate birthday,
        Gender gender
) {
    public static MyInfoResponse from(User myInfo) {
        return new MyInfoResponse(
                myInfo.getUserId(),
                myInfo.getEmail(),
                myInfo.getBirthDay(),
                myInfo.getGender()
        );
    }
}
