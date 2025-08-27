package com.loopers.domain.user;

public record UserActivityEvent(
        String traceId,
        String userId,
        String method,
        String uri,
        String route,
        int status
) {
}
