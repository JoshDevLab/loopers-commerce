package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserInfo;

public class UserContext {
    private static final ThreadLocal<UserInfo> userIdHolder = new ThreadLocal<>();

    public static void set(UserInfo userInfo) {
        userIdHolder.set(userInfo);
    }

    public static UserInfo get() {
        return userIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
