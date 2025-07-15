package com.loopers.interfaces.api.user;

public class UserContext {
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();

    public static void set(String userId) {
        userIdHolder.set(userId);
    }

    public static String get() {
        return userIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
