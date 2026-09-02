package com.nhnacademy.front.account.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.front.account.dto.signup.RegisterRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import com.nhnacademy.front.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String getSignupPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest("", "", "", ""));
        }
        return "account/signup"; // signup.html 호출
    }

    // 2. 회원가입 폼 제출 처리 (POST /signup)
    @PostMapping("/signup")
    public String signUp(
            @Valid @ModelAttribute("registerRequest") RegisterRequest requestDto,
            BindingResult bindingResult,
            Model model
    ) {
        // DTO 검증 에러 발생 시 원래 폼으로 돌아감
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "account/signup";
        }

        try {
            accountService.signup(requestDto);
            log.info("Signup success loginId:{}", requestDto.loginId());
            return "redirect:/login";
        } catch (feign.FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                log.warn("Failed to signup (Client Error): {}", e.getMessage());
            } else {
                log.warn("Failed to signup", e);
            }
            String errorMessage = "회원가입에 실패했습니다. (중복된 아이디/이메일 등)";
            try {
                JsonNode node = new ObjectMapper().readTree(e.contentUTF8());
                if (node.has("message")) errorMessage = node.get("message").asText();
            } catch (Exception ex) {
                log.warn("Error parsing feign exception", ex);
            }
            model.addAttribute("errorMessage", errorMessage);
            return "account/signup";
        }
    }

    @GetMapping("/mypage")
    public String getMyPage(
            @ModelAttribute("userId") Long userId,
            Model model
    ) {
        if (userId == null) {
            return "redirect:/login";
        }
        UserResponse myInfo = accountService.getUser(userId);
        model.addAttribute("myInfo", myInfo);

        return "mypage/profile";
    }
}