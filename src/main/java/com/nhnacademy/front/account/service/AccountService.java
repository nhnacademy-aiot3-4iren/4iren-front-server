package com.nhnacademy.front.account.service;

import com.nhnacademy.front.account.client.AccountClient;
import com.nhnacademy.front.account.dto.signup.SignupRequest;
import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponseDto;
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
    public void signup(SignupRequest requestDto){
        log.info("account-api로 회원가입 요청 전송 - loginId:{} ", requestDto.loginId());
        accountClient.signup(requestDto);
    }

    //회원 정보 조회(마이페이지 등)
    public UserResponseDto getUser(Long userId, Long requesterId) {
        return accountClient.getUser(userId, requesterId);
    }

    //회원 정보 수정
    public UserResponseDto updateUser(Long userId, Long requesterId, UpdateRequest requestDto) {
        return accountClient.updateUser(userId, requesterId, requestDto);
    }

    //회원 탈퇴
    public void withdraw(Long userId, Long requesterId) {
        accountClient.withdraw(userId, requesterId);
    }
}
