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
