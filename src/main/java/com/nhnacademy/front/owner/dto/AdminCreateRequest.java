package com.nhnacademy.front.owner.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AdminCreateRequest(
        @NotBlank(message = "아이디는 필수입니다")
        @Length(max=50, message="아이디는 50자 이하로 입력해주세요")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Length(min=8, max=100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요")
        String password,

        @NotBlank(message = "이름은 필수입니다")
        @Length(max=50, message = "이름은 50자 이하로 입력해주세요")
        String name
) {}
