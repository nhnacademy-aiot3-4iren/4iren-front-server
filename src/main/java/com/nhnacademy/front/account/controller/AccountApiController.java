package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import com.nhnacademy.front.account.service.AccountApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountApiService accountApiService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @ModelAttribute("userId") Long userId,
            @RequestBody UpdateRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        UserResponse response = accountApiService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> withdraw(
            @ModelAttribute("userId") Long userId,
            HttpServletResponse response
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        accountApiService.withdraw(userId);

        // 토큰 쿠키 삭제
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok().build();
    }
}
