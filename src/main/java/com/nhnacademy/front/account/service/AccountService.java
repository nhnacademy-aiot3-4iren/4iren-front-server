package com.nhnacademy.front.account.service;

import com.nhnacademy.front.account.client.AccountClient;
import com.nhnacademy.front.account.dto.signup.RegisterRequest;
import com.nhnacademy.front.account.dto.user.ResetPasswordRequest;
import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 회원가입,내 정보 조회, 회원정보 수정 로직
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountClient accountClient;

    //회원가입
    public void signup(RegisterRequest request){
        log.info("account-api로 회원가입 요청 전송 - loginId:{} ", request.loginId());
        accountClient.signup(request);
    }

    //회원 정보 조회(마이페이지 등)
    public UserResponse getUser(Long userId) {
        return accountClient.getUser(userId);
    }

    //임시 비밀번호 발급
    public void resetPassword(ResetPasswordRequest request) {
        accountClient.resetPassword(request);
    }
}
