package com.nhnacademy.front.payment.client;

import com.nhnacademy.front.payment.dto.PaymentHistoryResponse;
import com.nhnacademy.front.payment.dto.PlanPriceResponse;
import com.nhnacademy.front.payment.dto.StartRegistrationRequest;
import com.nhnacademy.front.payment.dto.StartRegistrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 게이트웨이를 거쳐 payment-api와 통신하는 통로
@FeignClient(name = "4iren-gateway", path = "/api/payment", contextId = "paymentClient")
public interface PaymentClient {

    @GetMapping("/plans")
    List<PlanPriceResponse> getPlans();

    @PostMapping("/billing-keys/toss/registrations")
    StartRegistrationResponse startTossRegistration(@RequestBody StartRegistrationRequest request);

    @PostMapping("/billing-keys/kakao/registrations")
    StartRegistrationResponse startKakaoRegistration(@RequestBody StartRegistrationRequest request);

    // 토스 콜백 relay - 인증 토큰 없이 게이트웨이의 인증 예외 라우트를 탄다
    @PostMapping("/billing-keys/toss/callback")
    ResponseEntity<Void> relayTossCallback(@RequestBody Map<String, String> callbackParams);

    // 카카오 콜백 relay - 사용자 브라우저의 세션 쿠키를 그대로 타고 가는 요청이라 인증 라우트를 정상 통과
    @GetMapping("/billing-keys/kakao/callback")
    ResponseEntity<Void> relayKakaoCallback(@RequestParam("pg_token") String pgToken,
                                             @RequestParam("orderId") String orderId);

    @DeleteMapping("/subscriptions")
    void cancelSubscription();

    @GetMapping("/payments")
    List<PaymentHistoryResponse> getPaymentHistory();

    @PostMapping("/billing-keys/toss/registrations/change")
    StartRegistrationResponse startTossChange();

    @PostMapping("/billing-keys/kakao/registrations/change")
    StartRegistrationResponse startKakaoChange();
}
