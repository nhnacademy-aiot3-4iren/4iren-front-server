package com.nhnacademy.front.auth.config;

import com.nhnacademy.front.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 인증 쿠키(accessToken / refreshToken) 유무를 확인하는 인터셉터
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(@Lazy AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        Cookie[] cookies = request.getCookies();
        boolean hasAccessToken = false;
        boolean hasRefreshToken = false;
        String refreshTokenValue = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    hasAccessToken = true;
                }
                if ("refreshToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    hasRefreshToken = true;
                    refreshTokenValue = cookie.getValue();
                }
            }
        }

        // accessToken은 없지만 refreshToken은 있는 경우 -> 자동 갱신 시도
        if (!hasAccessToken && hasRefreshToken) {
            log.info("AccessToken이 만료/삭제되어 RefreshToken으로 갱신을 시도합니다. URI: {}", request.getRequestURI());
            boolean refreshed = authService.refresh(refreshTokenValue, response);
            if (refreshed) {
                log.info("토큰 갱신 성공!");
                // 갱신 성공 시 원래 가려던 컨트롤러로 정상 진행
                hasAccessToken = true;
            } else {
                log.warn("토큰 갱신 실패. 다시 로그인해야 합니다.");
                hasRefreshToken = false; // 갱신 실패 시 둘 다 없는 것으로 간주하여 로그인 페이지로 보냄
            }
        }

        // 인증 쿠키가 모두 없으면 API는 401, HTML 페이지는 로그인 화면으로 응답한다.
        if (!hasAccessToken && !hasRefreshToken) {
            String requestUri = request.getRequestURI();
            if (requestUri.startsWith("/api/front/")) {
                log.info("인증 쿠키가 없는 Front API 요청을 거부합니다. URI: {}", requestUri);
                response.sendError(HttpStatus.UNAUTHORIZED.value());
            } else {
                log.info("인증 쿠키가 존재하지 않음. 로그인 페이지로 이동합니다. URI: {}", requestUri);
                response.sendRedirect("/login");
            }
            return false; // 컨트롤러 실행 중단
        }

        return true; // 요청 정상 진행
    }
}
