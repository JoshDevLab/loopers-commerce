package com.loopers.interfaces.interceptor;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;

@RequiredArgsConstructor
@Component
public class UserIdInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private static final String HEADER_USER_ID = "X-USER-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String userId = request.getHeader(HEADER_USER_ID);

        if (userId == null || userId.isBlank()) {
            errorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing X-USER-ID header", 400);
            return false;
        }

        try {
            User user = userService.getMyInfoByUserId(userId);
            UserInfo userInfo = UserInfo.of(user);
            UserContext.set(userInfo);
            return true;
        } catch (CoreException e) {
            errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage(), 401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void errorResponse(HttpServletResponse response, int statusCode, String message, int code) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        String body = String.format("""
                {
                  "error": "%s",
                  "code": %d,
                  "timestamp": "%s"
                }
                """, message, code, Instant.now());

        response.getWriter().write(body);
    }
}
