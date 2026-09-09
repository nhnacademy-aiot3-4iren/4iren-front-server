package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.dto.user.UpdateRequest;
import com.nhnacademy.front.account.dto.user.UserResponse;
import com.nhnacademy.front.account.service.AccountApiService;
import com.nhnacademy.front.payment.dto.SubscriptionResponse;
import com.nhnacademy.front.payment.service.PaymentService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.nhnacademy.front.payment.dto.SubscriptionStatus.ACTIVE;
import static com.nhnacademy.front.payment.dto.SubscriptionStatus.PAST_DUE;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountApiService accountApiService;
    private final PaymentService paymentService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @ModelAttribute("userId") Long userId,
            @RequestBody UpdateRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        UserResponse response = accountApiService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> withdraw(
            @ModelAttribute("userId") Long userId,
            HttpServletResponse response
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        try{
            SubscriptionResponse sub = paymentService.getCurrentSubscription();
            if(sub.status()==ACTIVE || sub.status() == PAST_DUE){
                return ResponseEntity.status(409).body(Map.of("message", "이용 중인 구독이 있어 탈퇴할 수 없습니다. 먼저 구독을 해지해주세요."));
            }
        }catch (FeignException e){
            log.warn("구독 상태 확인 실패", e);
        }
        
        accountApiService.withdraw(userId);

        // 토큰 쿠키 삭제
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok().build();
    }
}
