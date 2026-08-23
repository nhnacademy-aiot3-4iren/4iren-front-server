package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.dto.signup.SignupRequest;
import com.nhnacademy.front.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 1. 회원가입 페이지 HTML 띄우기 (GET /signup)
    @GetMapping("/signup")
    public String getSignupPage() {
        return "signup"; // signup.html 호출
    }

    // 2. 회원가입 폼 제출 처리 (POST /signup)
    @PostMapping("/signup")
    public String Signup(@ModelAttribute SignupRequest requestDto) {
        // AccountService 통해 account-api로 회원가입 요청 전송
        accountService.signup(requestDto);
        log.info("Signup success loginId:{}",requestDto.loginId());

        // 회원가입 성공 시 로그인 페이지로 리다이렉트
        return "redirect:/login";
    }
}