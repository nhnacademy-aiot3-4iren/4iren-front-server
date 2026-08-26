package com.nhnacademy.front.core.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.context.annotation.Bean;
import feign.Retryer;

@Configuration
public class FeignConfig implements RequestInterceptor {

    @Bean
    public Retryer retryer() {
        // 토큰 갱신 시 재시도를 허용하기 위해 최소한의 Retryer 활성화
        // 1초 간격으로 최대 2번 재시도 (최초 1번 실패 + 1번 재시도)
        return new Retryer.Default(1000, 2000, 2);
    }
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 1. 방금 갱신되어서 request attribute에 저장된 토큰이 있는지 먼저 확인
            String newAccessToken = (String) request.getAttribute("newAccessToken");
            if (newAccessToken != null && !newAccessToken.isEmpty()) {
                template.removeHeader("Authorization"); // 기존 실패한 토큰 헤더 삭제
                template.header("Authorization", "Bearer " + newAccessToken);
                return;
            }

            // 2. 없으면 기존 쿠키에서 확인
            Cookie[] cookies = request.getCookies();
            if (cookies != null && !template.headers().containsKey("Authorization")) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                        template.header("Authorization", "Bearer " + cookie.getValue());
                        break;
                    }
                }
            }
        }
    }
}
