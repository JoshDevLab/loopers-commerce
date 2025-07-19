package com.loopers.domain.user;

import lombok.Getter;

@Getter
public class UserCommand {

    public record Register(
            String userId,
            String email,
            String birthday,
            String gender
    ) {
    }

}
