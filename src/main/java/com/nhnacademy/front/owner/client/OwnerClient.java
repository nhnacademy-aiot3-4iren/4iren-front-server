package com.nhnacademy.front.owner.client;

import com.nhnacademy.front.owner.dto.AdminCreateRequest;
import com.nhnacademy.front.owner.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "4iren-gateway", contextId = "ownerClient", path = "/api/account/owner")
public interface OwnerClient {

    // 관리자 생성
    @PostMapping("/signup")
    ResponseEntity<Void> signUp(@RequestBody AdminCreateRequest request);

    // 관리자 목록 조회
    @GetMapping("/list")
    ResponseEntity<List<UserResponse>> getUsers();

    // 관리자 상세 조회
    @GetMapping("/{user-id}")
    ResponseEntity<UserResponse> getUser(@PathVariable("user-id") Long userId);

    // 관리자 삭제
    @PatchMapping("/{user-id}")
    ResponseEntity<Void> deleteUser(@PathVariable("user-id") Long userId);

    // 관리자 복구
    @PatchMapping("/{user-id}/restore")
    ResponseEntity<Void> restoreUser(@PathVariable("user-id") Long userId);

    // 비밀번호 초기화
    @PatchMapping("/{user-id}/reset-password")
    ResponseEntity<Void> resetPassword(@PathVariable("user-id") Long userId);
}
