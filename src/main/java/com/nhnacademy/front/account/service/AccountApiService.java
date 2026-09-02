package com.nhnacademy.front.account.service;

import com.nhnacademy.front.account.client.AccountClient;
import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountApiService {
    private final AccountClient accountClient;

    //회원 정보 수정
    public UserResponse updateUser(Long userId, UpdateRequest requestDto) {
        return accountClient.updateUser(userId, requestDto);
    }

    //회원 탈퇴
    public void withdraw(Long userId) {
        accountClient.withdraw(userId);
    }
}
