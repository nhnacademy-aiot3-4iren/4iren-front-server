package com.nhnacademy.front.account.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import com.nhnacademy.front.account.service.AccountApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        try {
            UserResponse response = accountApiService.updateUser(userId, request);
            return ResponseEntity.ok(response);
        } catch (feign.FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                log.warn("Failed to update profile (Client Error): {}", e.getMessage());
            } else {
                log.error("Failed to update profile", e);
            }
            String errorMessage = "수정에 실패했습니다.";
            try {
                JsonNode node = new ObjectMapper().readTree(e.contentUTF8());
                if (node.has("message")) {
                    errorMessage = node.get("message").asText();
                }
            } catch (Exception ex) {
                log.warn("Error parsing feign exception", ex);
            }
            return ResponseEntity.status(e.status()).body(Map.of("message", errorMessage));
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> withdraw(
            @ModelAttribute("userId") Long userId,
            HttpServletResponse response
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        try {
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
        } catch (feign.FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                log.warn("Failed to withdraw (Client Error): {}", e.getMessage());
            } else {
                log.error("Failed to withdraw", e);
            }
            String errorMessage = "탈퇴 처리에 실패했습니다.";
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(e.contentUTF8());
                if (node.has("message")) {
                    errorMessage = node.get("message").asText();
                }
            } catch (Exception ex) {
                log.warn("Error parsing feign exception", ex);
            }
            return ResponseEntity.status(e.status()).body(Map.of("message", errorMessage));
        }
    }
}
