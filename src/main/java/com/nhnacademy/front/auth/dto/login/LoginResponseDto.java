package com.nhnacademy.front.auth.dto.login;

// 로그인 응답 DTO
public record LoginResponseDto(
        Long userId,
        String userLoginId,
        String userName,
        String userRole
) {}
