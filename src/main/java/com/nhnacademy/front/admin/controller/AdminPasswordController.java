package com.nhnacademy.front.admin.controller;

import com.nhnacademy.front.account.client.AccountClient;
import com.nhnacademy.front.account.dto.user.UpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/change-password")
@RequiredArgsConstructor
public class AdminPasswordController {

    private final AccountClient accountClient;

    @GetMapping
    public String changePasswordPage() {
        return "admin/change_password";
    }

    @PostMapping
    public String doChangePassword(
            @ModelAttribute("userId") Long userId,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        if (userId == null) {
            return "redirect:/login";
        }

        if (password == null || password.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호는 8자 이상이어야 합니다.");
            return "redirect:/admin/change-password";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/admin/change-password";
        }

        try {
            UpdateRequest request = new UpdateRequest(null, null, password);
            accountClient.updateUser(userId, request);
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다. 메인 페이지로 이동합니다.");
            return "redirect:/";
        } catch (Exception e) {
            log.error("Failed to change password for admin userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 변경에 실패했습니다.");
            return "redirect:/admin/change-password";
        }
    }
}
