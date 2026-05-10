package com.config;

import com.controller.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/pois/**",
                        "/rects/**",
                        "/contracts/**"
                )
                .excludePathPatterns(
                        "/",
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/users/login",
                        "/users/logout"
                );
    }
}
