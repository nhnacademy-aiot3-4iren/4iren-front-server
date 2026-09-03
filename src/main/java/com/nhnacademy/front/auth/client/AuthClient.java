package com.nhnacademy.front.auth.client;

import com.nhnacademy.front.auth.dto.login.LoginRequest;
import com.nhnacademy.front.auth.dto.token.TokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="4iren-gateway",contextId = "authClient",path="/api/auth")
public interface AuthClient {
    //로그인 요청
    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request);

    //로그아웃 요청
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader);

    //토큰 재발급 요청
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refreshToken(
            @CookieValue(value = "refreshToken",required = false) String refreshToken
    );
}
