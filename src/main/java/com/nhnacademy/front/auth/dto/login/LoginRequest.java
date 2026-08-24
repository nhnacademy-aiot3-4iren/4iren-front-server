package com.nhnacademy.front.auth.dto.login;

import jakarta.validation.constraints.NotBlank;

import java.beans.ConstructorProperties;


// 로그인 요청 DTO
public record LoginRequest (
    @NotBlank
    String loginId,

    @NotBlank
    String password
) {
    @ConstructorProperties({"loginId", "password"})
    public LoginRequest {}
}
