package com.loopers.interfaces.config;

import com.loopers.interfaces.interceptor.CurrentUserIdArgumentResolver;
import com.loopers.interfaces.interceptor.OptionalUserIdInterceptor;
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

    public static final String[] OPTIONAL_AUTH_PATHS = {
            // "목록"과 "상세"만 비로그인 허용
            "/api/v1/products",             // 목록 조회
            "/api/v1/products/",            // 일부 브라우저에서 슬래시 포함 요청 방지
            "/api/v1/products/{productId:[\\d]+}", // 상세 조회만 허용
            "/api/v1/brands/**"
    };

    public static final String[] LOGIN_EXCLUDE_PATHS = {
            "/api/v1/products",
            "/api/v1/products/",
            "/api/v1/products/{productId:[\\d]+}", // 상세
            "/api/v1/brands/**",
            "/api/v1/users",
            "/health",
            "/docs/**"
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
    }
}
