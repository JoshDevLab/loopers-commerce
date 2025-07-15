package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String userId = request.getHeader("X-USER-ID");

        if (userId == null || userId.isBlank()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "X-USER-ID header 가 존재하지 않습니다.");
            return false;
        }

        User user = userService.getMyInfo(userId);

        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, userId + "는 존재하지 않는 유저입니다.");
            return false;
        }

        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        String jsonError = """
            {
              "error": "%s",
              "code": %d
            }
            """.formatted(errorMessage, statusCode);

        response.getWriter().write(jsonError);
    }
}
