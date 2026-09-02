package com.nhnacademy.front.controller;

import com.nhnacademy.front.payment.client.PaymentClient;
import com.nhnacademy.front.payment.dto.PaymentHistoryResponse;
import com.nhnacademy.front.payment.dto.Plan;
import com.nhnacademy.front.payment.dto.StartRegistrationRequest;
import com.nhnacademy.front.payment.dto.StartRegistrationResponse;
import com.nhnacademy.front.payment.dto.SubscriptionResponse;
import com.nhnacademy.front.payment.dto.SubscriptionStatus;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentPageController {
    private static final String OWNER = "OWNER";
    private static final String ADMIN = "ADMIN";
    private static final String NORMAL = "NORMAL";

    private final PaymentClient paymentClient;

    @GetMapping("/payment/plans")
    public String getPlanSelectionPage(@ModelAttribute("role") String role, Model model) {
        if (ADMIN.equals(role)) {
            return "redirect:/mypage";
        }
        model.addAttribute("plans", paymentClient.getPlans());
        model.addAttribute("alreadySubscribed", OWNER.equals(role));
        return "payment/plan-select";
    }

    @GetMapping("/payment")
    public String getPaymentPage(@ModelAttribute("role") String role, Model model) {
        if(ADMIN.equals(role) || NORMAL.equals(role)) {
            return "redirect:/mypage";
        }

        model.addAttribute("role", role);
        List<PaymentHistoryResponse> payments = paymentClient.getPaymentHistory();
        model.addAttribute("payments", payments);
        model.addAttribute("lastPaymentDate", findLastPaymentDate(payments));

        try {
            SubscriptionResponse subscription = paymentClient.getCurrentSubscription();
            model.addAttribute("currentPlanName", planDisplayName(subscription.plan()));
            model.addAttribute("billingCycle", billingCycleText(subscription.plan()));
            model.addAttribute("subscriptionStatusText", statusDisplayText(subscription.status()));
            model.addAttribute("subscriptionAmount", subscription.amount());
            model.addAttribute("nextBillingDate", subscription.nextBillingDate());
        } catch (FeignException e) {
            log.error("현재 구독 정보 조회 실패", e);
        }

        return "sidebar-menu/settings/payment";
    }

    // payments는 이미 최신순으로 오므로 status가 DONE인 첫 건의 승인/시도 일시를 사용
    private LocalDateTime findLastPaymentDate(List<PaymentHistoryResponse> payments) {
        return payments.stream()
                .filter(p -> "DONE".equals(p.status()))
                .findFirst()
                .map(p -> p.approvedAt() != null ? p.approvedAt() : p.attemptedAt())
                .orElse(null);
    }

    private String planDisplayName(Plan plan) {
        return switch (plan) {
            case MONTHLY -> "월간 요금제";
            case YEARLY -> "연간 요금제";
        };
    }

    private String billingCycleText(Plan plan) {
        return switch (plan) {
            case MONTHLY -> "월간 결제";
            case YEARLY -> "연간 결제";
        };
    }

    private String statusDisplayText(SubscriptionStatus status) {
        return switch (status) {
            case ACTIVE -> "정상 이용 중";
            case PAST_DUE -> "결제 재시도 중";
            case CANCELED -> "해지 예정";
            case EXPIRED -> "만료됨"; // OWNER만 접근 가능한 화면이라 실제로는 안 나오는 상태값
        };
    }

    @PostMapping("/payment/registrations/toss")
    public String startToss(@ModelAttribute("role") String role,
                            @RequestParam Plan plan,
                            RedirectAttributes redirectAttributes) {
        if (!NORMAL.equals(role)) {
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
        if (!NORMAL.equals(role)) {
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


    @PostMapping("/payment/billing/change/toss")
    public String changeToss(RedirectAttributes redirectAttributes) {
        try {
            StartRegistrationResponse start = paymentClient.startTossChange();
            return "redirect:" + start.redirectUrl();
        } catch (FeignException e) {
            log.error("토스 결제수단 변경 시작 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제수단 변경 시작에 실패했습니다.");
            return "redirect:/payment";
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
            return "redirect:/payment";
        }
    }

    @PostMapping("/payment/billing/cancel")
    public String cancelSubscription(RedirectAttributes redirectAttributes) {
        try {
            paymentClient.cancelSubscription();
            redirectAttributes.addFlashAttribute("successMessage", "구독이 해지되었습니다. 다음 결제일까지는 계속 이용하실 수 있습니다.");
        } catch (FeignException e) {
            log.error("구독 해지 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "구독 해지에 실패했습니다.");
        }
        return "redirect:/payment";
    }

    @GetMapping("/payment/success")
    public String getPaymentSuccessPage() {
        return "payment/success";
    }

    @GetMapping("/payment/failure")
    public String getPaymentFailurePage() {
        return "payment/failure";
    }
}