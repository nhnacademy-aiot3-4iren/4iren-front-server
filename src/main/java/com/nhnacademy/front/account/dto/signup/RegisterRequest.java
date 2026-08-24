package com.nhnacademy.front.account.dto.signup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.beans.ConstructorProperties;

// 회원가입 요청 DTO
public record RegisterRequest(
    @NotBlank(message = "아이디는 필수입니다")
    String loginId,

    @NotBlank(message = "이메일은 필수입니다")
    @Email
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    String password,

    @NotBlank(message = "이름은 필수입니다")
    String name
) {
    @ConstructorProperties({"loginId", "email", "password", "name"})
    public RegisterRequest{}
}
