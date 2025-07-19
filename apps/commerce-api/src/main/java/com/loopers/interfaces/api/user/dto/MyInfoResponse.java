package com.loopers.interfaces.api.user.dto;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;

import java.time.LocalDate;

public record MyInfoResponse(
        String userId,
        String email,
        LocalDate birthday,
        Gender gender
) {
    public static MyInfoResponse from(UserInfo myInfo) {
        return new MyInfoResponse(
                myInfo.userId(),
                myInfo.email(),
                myInfo.birthday(),
                myInfo.gender()
        );
    }
}
