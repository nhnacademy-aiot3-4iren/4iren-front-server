package com.nhnacademy.front.payment.controller;

import com.nhnacademy.front.payment.client.PaymentClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final PaymentClient paymentClient;

    // 토스는 서버-투-서버라 받은 상태코드를 그대로 돌려주면 됨 - 리다이렉트 대상 없음
    @PostMapping("/callback/toss")
    public ResponseEntity<Void> tossCallback(@RequestBody Map<String, String> callbackParams) {
        try {
            return paymentClient.relayTossCallback(callbackParams);
        } catch (FeignException e) {
            log.error("토스 콜백 payment-api relay 실패 - callbackParams={}", callbackParams, e);
            return ResponseEntity.status(e.status()).build();
        }
    }

    // 카카오는 사용자 브라우저가 직접 오는 요청이라, 처리 후 실제로 브라우저를 리다이렉트해야 함
    @GetMapping("/callback/kakao")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("pg_token") String pgToken,
                                               @RequestParam("orderId") String orderId) {
        try {
            paymentClient.relayKakaoCallback(pgToken, orderId);
        } catch (FeignException e) {
            log.error("카카오 콜백 payment-api relay 실패 - orderId={}", orderId, e);
        }
        // 성공/실패 페이지가 아직 없어서 일단 마이페이지로 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/mypage")).build();
    }
}
