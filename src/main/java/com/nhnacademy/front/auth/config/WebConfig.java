package com.nhnacademy.front.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 인터셉터 등록 및 경로 제외 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 모든 경로에 대해 인터셉터 적용
                .excludePathPatterns(
                        "/", "/login", "/logout", "/signup",    // 로그인/가입 관련 경로는 제외
                        "/css/**", "/js/**", "/photo/**", "/images/**",
                        "/favicon.ico", "/favicon-16x16.png", "/favicon-32x32.png",
                        "/favicon-48x48.png", "/apple-touch-icon.png", "/site.webmanifest",
                        "/error", // 정적 파일 제외
                        "/.well-known/**", "/fonts/**",
                        "/callback/**", // PG 콜백 - Toss는 PG 서버가 직접 호출(쿠키 없음), 인가는 payment-api의 pending 상관관계 체크가 담당
                        "/payment/plans",
                        "/video/**" // video 관련 출력 허용
                );
    }
}
