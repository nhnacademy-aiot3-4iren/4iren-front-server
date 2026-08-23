package com.nhnacademy.front.auth.dto.login;

import jakarta.validation.constraints.NotBlank;

// 로그인 요청 DTO
public record LoginRequestDto(
    @NotBlank
    String userLoginId,

    @NotBlank
    String userPassword
) {}
