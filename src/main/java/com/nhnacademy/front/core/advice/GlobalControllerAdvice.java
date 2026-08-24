package com.nhnacademy.front.core.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ObjectMapper objectMapper;

    @ModelAttribute("loginId")
    public String getLoginId(HttpServletRequest request) {
        return extractClaim(request, "login-id");
    }

    @ModelAttribute("userId")
    public Long getUserId(HttpServletRequest request) {
        String sub = extractClaim(request, "sub");
        return (sub != null && !sub.isEmpty()) ? Long.valueOf(sub) : null;
    }

    @ModelAttribute("role")
    public String getRole(HttpServletRequest request) {
        String role = extractClaim(request, "role");
        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        return role;
    }

    private String extractClaim(HttpServletRequest request, String claimKey) {
        String token = (String) request.getAttribute("newAccessToken");

        if (token == null || token.isEmpty()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token != null && !token.isEmpty()) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            try {
                String[] parts = token.split("\\.");
                if (parts.length > 1) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    JsonNode jsonNode = objectMapper.readTree(payload);
                    if (jsonNode.has(claimKey)) {
                        return jsonNode.get(claimKey).asText();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to decode JWT payload: {}", e.getMessage());
            }
        }
        return null;
    }
}
