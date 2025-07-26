package com.loopers.interfaces.interceptor;

import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.logging.Logger;

@RequiredArgsConstructor
@Component
public class OptionalUserIdInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private static final String HEADER_USER_ID = "X-USER-ID";
    private final Logger logger = Logger.getLogger(OptionalUserIdInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HEADER_USER_ID);

        if (userId != null && !userId.isBlank()) {
            try {
                UserInfo userInfo = UserInfo.of(userService.getMyInfoByUserId(userId));
                UserContext.set(userInfo);
            } catch (CoreException e) {
                // 유효하지 않은 userId는 무시 → 비로그인 사용자로 간주
                logger.info("유효하지 않은 userId는 무시 → 비로그인 사용자로 간주");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
