package com.nhnacademy.front.auth.controller;

import com.nhnacademy.front.auth.dto.login.LoginRequestDto;
import com.nhnacademy.front.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. 루트 (/) 접속 시 로그인 페이지로 리다이렉트
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // 2. 로그인 페이지 HTML 띄우기 (GET /login)
    @GetMapping("/login")
    public String loginPage(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        // 이미 로그인된 상태(accessToken 쿠키가 존재하는 경우)라면 메인 페이지로 이동
        if (accessToken != null && !accessToken.isEmpty()) {
            return "redirect:/";
        }
        return "account/login"; // templates/account/login.html 호출
    }

    // 3. 로그인 폼 제출 처리 (POST /login)
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute LoginRequestDto requestDto,
            HttpServletResponse response
    ) {
        try {
            // AuthService를 통해 로그인 및 HttpOnly 쿠키 생성
            authService.login(requestDto, response);
            log.info("Login success for user: {}", requestDto.userLoginId());
            return "redirect:/";
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return "redirect:/login?error";
        }
    }

    // 4. 로그아웃 처리 (POST /logout)
    @PostMapping("/logout")
    public String logout(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpServletResponse response
    ) {
        // AuthService를 통해 로그아웃 및 쿠키 삭제
        authService.logout(accessToken, response);
        log.info("Logout completed");
        return "redirect:/login";
    }
}
