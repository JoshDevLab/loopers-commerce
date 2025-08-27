package com.loopers.interfaces.interceptor;

import com.loopers.domain.user.UserActivityEvent;
import com.loopers.interfaces.event.user.UserActivityEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UserActivityInterceptor implements HandlerInterceptor {
    private final UserActivityEventPublisher publisher;

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {

        String traceId = Optional.ofNullable(request.getHeader("X-TRACE-ID"))
                .orElse(Optional.ofNullable(MDC.get("traceId"))
                        .orElse(UUID.randomUUID().toString()));

        String userId = Optional.ofNullable(MDC.get("userId"))
                .orElse(request.getHeader("X-USER-ID")); // 없으면 null

        String route = (handler instanceof HandlerMethod hm)
                ? hm.getMethod().getDeclaringClass().getSimpleName() + "#" + hm.getMethod().getName()
                : "unknown";

        UserActivityEvent event = new UserActivityEvent(
                traceId,
                userId,
                request.getMethod(),
                request.getRequestURI(),
                route,
                response.getStatus()
        );

        publisher.publish(event);
    }
}
