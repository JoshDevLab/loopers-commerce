package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;

public class UserContext {
    private static final ThreadLocal<User> userIdHolder = new ThreadLocal<>();

    public static void set(User user) {
        userIdHolder.set(user);
    }

    public static User get() {
        return userIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
