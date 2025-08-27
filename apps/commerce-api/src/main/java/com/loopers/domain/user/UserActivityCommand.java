package com.loopers.domain.user;

public record UserActivityCommand(
        String traceId,
        String userId,
        String method,
        String uri,
        String route,
        int status
) {
    public static UserActivityCommand create(String traceId, String userId, String method, String uri, String route, int status) {
        return new UserActivityCommand(traceId, userId, method, uri, route, status);
    }
}
