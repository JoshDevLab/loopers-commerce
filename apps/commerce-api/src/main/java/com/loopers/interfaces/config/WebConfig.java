package com.loopers.interfaces.config;

import com.loopers.interfaces.interceptor.CurrentUserIdArgumentResolver;
import com.loopers.interfaces.interceptor.OptionalUserIdInterceptor;
import com.loopers.interfaces.interceptor.UserActivityInterceptor;
import com.loopers.interfaces.interceptor.UserIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver resolver;
    private final UserIdInterceptor userIdInterceptor;
    private final OptionalUserIdInterceptor optionalUserIdInterceptor;
    private final UserActivityInterceptor userActivityInterceptor;

    public static final String[] OPTIONAL_AUTH_PATHS = {
            "/api/v1/products/**",
            "/api/v1/brands/**"
    };

    public static final String[] LOGIN_EXCLUDE_PATHS = {
            "/api/v1/products/**",
            "/api/v1/brands/**",
            "/api/v1/users",
            "/health",
            "/docs/**"
    };

    public static final String[] USER_ACTIVITY_TRACKING_PATHS = {
            "/api/v1/products/*",           // 단일 상품 조회
            "/api/v1/products",             // 상품 목록 조회
            "/api/v1/like/products/*",      // 좋아요/좋아요 취소
            "/api/v1/orders",               // 주문 생성, 주문 목록 조회
            "/api/v1/orders/*",             // 주문 상세 조회
            "/api/v1/payments/**"           // 결제 관련 모든
    };

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(optionalUserIdInterceptor)
                .addPathPatterns(OPTIONAL_AUTH_PATHS);

        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(LOGIN_EXCLUDE_PATHS);

        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns(USER_ACTIVITY_TRACKING_PATHS)
                .excludePathPatterns(
                        "/health", "/actuator/**", "/docs/**",
                        "/static/**", "/favicon.ico", "/error"
                );
    }
}
