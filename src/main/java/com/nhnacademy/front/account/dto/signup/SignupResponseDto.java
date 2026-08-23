package com.nhnacademy.front.account.dto.signup;

import lombok.Getter;
import lombok.NoArgsConstructor;

//회원가입 완료 응답 dto
@Getter
@NoArgsConstructor
public class SignupResponseDto {
    private Long userId;
    private String loginId;
}
