package com.nhnacademy.front.auth.service;

import com.nhnacademy.front.auth.client.AuthClient;
import com.nhnacademy.front.auth.dto.login.LoginRequestDto;
import com.nhnacademy.front.auth.dto.token.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthClient authClient;

    // 1. 로그인 처리 및 쿠키 세팅
    public void login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        log.info("Requesting login to auth-api for user: {}", loginRequestDto.userLoginId());

        // auth-api 로 로그인 요청
        ResponseEntity<TokenResponse> resp = authClient.login(loginRequestDto);

        // 1) auth-api에서 전달된 Set-Cookie 헤더(refreshToken 등)를 클라이언트 브라우저로 전달
        List<String> cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                response.addHeader(HttpHeaders.SET_COOKIE, cookie);
            }
        }

        // 2) 응답 본문에서 accessToken을 꺼내 HttpOnly 쿠키로 설정
        if (resp.getBody() != null && resp.getBody().accessToken() != null) {
            String accessToken = resp.getBody().accessToken();
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                    .path("/")
                    .httpOnly(true)
                    .secure(false) // 개발 환경: false, HTTPS: true
                    .sameSite("Lax")
                    .maxAge(60 * 60) // 1시간 유지
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        }
    }

    // 2. 로그아웃 처리 및 쿠키 삭제 (만료)
    public void logout(String accessToken, HttpServletResponse response) {
        // auth-api로 로그아웃 요청 (블랙리스트 처리)
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                authClient.logout("Bearer " + accessToken);
            } catch (Exception e) {
                log.warn("Auth-api logout failed: {}", e.getMessage());
            }
        }

        // accessToken 쿠키 즉시 만료 (MaxAge = 0)
        ResponseCookie ac = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, ac.toString());

        // refreshToken 쿠키 즉시 만료 (MaxAge = 0)
        ResponseCookie rc = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
    }
}
