package com.loopers.domain.user;

import lombok.Getter;

@Getter
public class UserRegisterCommand {
    private String userId;
    private String email;
    private String birthday;
    private Gender gender;

    public UserRegisterCommand(String userId, String email, String birthday, Gender gender) {
        this.userId = userId;
        this.email = email;
        this.birthday = birthday;
        this.gender  = gender;
    }

}
