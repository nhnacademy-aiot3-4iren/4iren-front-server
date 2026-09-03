package com.nhnacademy.front.controller;

import com.nhnacademy.front.payment.client.PaymentClient;
import com.nhnacademy.front.payment.dto.Plan;
import com.nhnacademy.front.payment.dto.StartRegistrationRequest;
import com.nhnacademy.front.payment.dto.StartRegistrationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentPageController {

    private final PaymentClient paymentClient;

    @GetMapping("/payment/plans")
    public String getPlanSelectionPage(@ModelAttribute("role") String role, Model model) {
        if ("ADMIN".equals(role)) {
            return "redirect:/mypage";
        }
        model.addAttribute("plans", paymentClient.getPlans());
        model.addAttribute("alreadySubscribed", "OWNER".equals(role));
        return "payment/plan-select";
    }

    @PostMapping("/payment/registrations/toss")
    public String startToss(@ModelAttribute("role") String role,
                            @RequestParam Plan plan,
                            RedirectAttributes redirectAttributes) {
        if (!"NORMAL".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 구독 중이거나 결제할 수 없는 계정입니다.");
            return "redirect:/payment/plans";
        }
        try {
            StartRegistrationResponse start = paymentClient.startTossRegistration(new StartRegistrationRequest(plan));
            return "redirect:" + start.redirectUrl();
        } catch (FeignException e) {
            log.error("토스 결제 시작 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 시작에 실패했습니다.");
            return "redirect:/payment/plans";
        }
    }

    @PostMapping("/payment/registrations/kakao")
    public String startKakao(@ModelAttribute("role") String role,
                             @RequestParam Plan plan,
                             RedirectAttributes redirectAttributes) {
        if (!"NORMAL".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 구독 중이거나 결제할 수 없는 계정입니다.");
            return "redirect:/payment/plans";
        }
        try {
            StartRegistrationResponse start = paymentClient.startKakaoRegistration(new StartRegistrationRequest(plan));
            return "redirect:" + start.redirectUrl();
        } catch (FeignException e) {
            log.error("카카오 결제 시작 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 시작에 실패했습니다.");
            return "redirect:/payment/plans";
        }
    }

    @GetMapping("/payment/billing")
    public String getBillingPage(Model model) {
        model.addAttribute("payments", paymentClient.getPaymentHistory());
        // 결제 내역엔 plan 정보가 없어서(PaymentHistoryResponse에 plan 필드 없음),
        // amount 기준으로 요금제를 역매칭하기 위해 plans도 같이 내려줌.
        // (뷰에서 최근 DONE 결제 건의 amount와 plans의 amount를 비교해서 plan명을 찾음)
        model.addAttribute("plans", paymentClient.getPlans());
        return "payment/billing";
    }

    @PostMapping("/payment/billing/change/toss")
    public String changeToss(RedirectAttributes redirectAttributes) {
        try {
            StartRegistrationResponse start = paymentClient.startTossChange();
            return "redirect:" + start.redirectUrl();
        } catch (FeignException e) {
            log.error("토스 결제수단 변경 시작 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제수단 변경 시작에 실패했습니다.");
            return "redirect:/payment/billing";
        }
    }

    @PostMapping("/payment/billing/change/kakao")
    public String changeKakao(RedirectAttributes redirectAttributes) {
        try {
            StartRegistrationResponse start = paymentClient.startKakaoChange();
            return "redirect:" + start.redirectUrl();
        } catch (FeignException e) {
            log.error("카카오 결제수단 변경 시작 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제수단 변경 시작에 실패했습니다.");
            return "redirect:/payment/billing";
        }
    }

    // 신규 추가: 구독 해지. PaymentClient.cancelSubscription()은 이미 있었으나
    // 이걸 실제로 호출해서 화면 버튼과 연결하는 엔드포인트가 없었음.
    @PostMapping("/payment/billing/cancel")
    public String cancelSubscription(RedirectAttributes redirectAttributes) {
        try {
            paymentClient.cancelSubscription();
            redirectAttributes.addFlashAttribute("successMessage", "구독이 해지되었습니다. 다음 결제일까지는 계속 이용하실 수 있습니다.");
        } catch (FeignException e) {
            log.error("구독 해지 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "구독 해지에 실패했습니다.");
        }
        return "redirect:/payment/billing";
    }

    @GetMapping("/payments/success")
    public String getPaymentSuccessPage() {
        return "payment/success";
    }

    @GetMapping("/payments/failure")
    public String getPaymentFailurePage() {
        return "payment/failure";
    }
}