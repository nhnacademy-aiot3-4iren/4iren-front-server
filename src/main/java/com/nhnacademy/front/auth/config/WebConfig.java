package com.nhnacademy.front.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * 인터셉터 등록 및 경로 제외 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final Duration MVC_ASYNC_TIMEOUT = Duration.ofMinutes(35);

    private final AuthInterceptor authInterceptor;

    private final VirtualThreadTaskExecutor mvcAsyncTaskExecutor =
            new VirtualThreadTaskExecutor("front-mvc-async-");

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
                        "/video/**", // video 관련 출력 허용
                        "/account/forgot" // 임시 비밀번호 설정 페이지
                );
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcAsyncTaskExecutor);
        // Core SSE 연결(30분)이 Front의 MVC async timeout보다 먼저 종료되도록 여유를 둔다.
        configurer.setDefaultTimeout(MVC_ASYNC_TIMEOUT.toMillis());
    }
}
