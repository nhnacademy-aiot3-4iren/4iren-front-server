package com.nhnacademy.front.account.client;

import com.nhnacademy.front.account.dto.signup.RegisterRequest;
import com.nhnacademy.front.account.dto.user.ResetPasswordRequest;
import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//백엔드 api와의 통신 통로
@FeignClient(name = "4iren-gateway", path = "/api/account", contextId = "accountClient")
public interface AccountClient {

    // 1. 회원가입
    @PostMapping("/signup")
    void signup(@RequestBody RegisterRequest requestDto);

    // 2. 회원 상세 정보 조회 (마이페이지 등)
    @GetMapping("/{user-id}")
    UserResponse getUser(
            @PathVariable("user-id") Long userId //대상 사용자 id
    );

    // 3. 회원 정보 수정
    @PutMapping("/{user-id}")
    UserResponse updateUser(
            @PathVariable("user-id") Long userId,
            @RequestBody UpdateRequest request
    );

    // 4. 회원 탈퇴
    @PatchMapping("/{user-id}")
    void withdraw(
            @PathVariable("user-id") Long userId
    );

    // 5. 임시 비밀번호 발급
    @PostMapping("/reset-password")
    void resetPassword(@RequestBody ResetPasswordRequest request);
}
