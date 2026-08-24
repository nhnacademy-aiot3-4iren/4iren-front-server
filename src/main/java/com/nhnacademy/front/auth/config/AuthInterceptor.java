package com.nhnacademy.front.auth.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 인증 쿠키(accessToken / refreshToken) 유무를 확인하는 인터셉터
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        Cookie[] cookies = request.getCookies();
        boolean hasAccessToken = false;
        boolean hasRefreshToken = false;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    hasAccessToken = true;
                }
                if ("refreshToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    hasRefreshToken = true;
                }
            }
        }

        // 인증 쿠키가 모두 없으면 로그인 페이지로 리다이렉트
        if (!hasAccessToken && !hasRefreshToken) {
            log.info("인증 쿠키가 존재하지 않음. 로그인 페이지로 이동합니다. URI: {}", request.getRequestURI());
            response.sendRedirect("/login");
            return false; // 컨트롤러 실행 중단
        }

        return true; // 요청 정상 진행
    }
}
