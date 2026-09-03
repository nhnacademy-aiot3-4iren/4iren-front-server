package com.nhnacademy.front.account.dto.user;

import jakarta.validation.constraints.Email;

import java.beans.ConstructorProperties;

// 회원 정보 수정 요청 DTO
public record UpdateRequest (
    String loginId,

    @Email(message = "이메일 형식이 올바르지 않습니다")
    String email,

    String password
) {
    @ConstructorProperties({"loginId", "email", "password"})
    public UpdateRequest {}
}
