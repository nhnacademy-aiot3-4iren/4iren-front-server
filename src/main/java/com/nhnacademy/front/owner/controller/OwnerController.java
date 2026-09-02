package com.nhnacademy.front.owner.controller;

import com.nhnacademy.front.account.config.FeignErrorParser;
import com.nhnacademy.front.owner.dto.AdminCreateRequest;
import com.nhnacademy.front.owner.dto.UserResponse;
import com.nhnacademy.front.owner.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/owner/admin")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/list")
    public String adminList(Model model) {
        try {
            List<UserResponse> admins = ownerService.getAdmins();
            model.addAttribute("admins", admins);
            if (!model.containsAttribute("adminCreateRequest")) {
                model.addAttribute("adminCreateRequest", new AdminCreateRequest("", "", ""));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch admin list", e);
            model.addAttribute("errorMessage", "관리자 목록을 불러오는데 실패했습니다.");
        }
        return "owner/admin_list";
    }

    @PostMapping("/create")
    public String createAdmin(
            @Valid @ModelAttribute AdminCreateRequest request,
            org.springframework.validation.BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/owner/admin/list";
        }

        try {
            ownerService.createAdmin(request);
            redirectAttributes.addFlashAttribute("successMessage", "관리자 계정이 성공적으로 생성되었습니다.");
        } catch (feign.FeignException e) {
            String errorMessage = FeignErrorParser.getMessage(e, "관리자 계정 생성에 실패했습니다.");
            log.warn("Failed to create admin", e);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        } catch (Exception e) {
            log.warn("Failed to create admin", e);
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류로 관리자 계정 생성에 실패했습니다.");
        }
        return "redirect:/owner/admin/list";
    }

    @GetMapping("/{user-id}")
    public String adminDetail(
            @PathVariable("user-id") Long userId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserResponse admin = ownerService.getAdmin(userId);
            model.addAttribute("admin", admin);
            return "owner/admin_detail";
        } catch (Exception e) {
            log.warn("Failed to fetch admin detail", e);
            redirectAttributes.addFlashAttribute("errorMessage", "관리자 상세 정보를 불러오는데 실패했습니다.");
            return "redirect:/owner/admin/list";
        }
    }

    @PostMapping("/{user-id}/delete")
    public String deleteAdmin(@PathVariable("user-id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            ownerService.deleteAdmin(userId);
            redirectAttributes.addFlashAttribute("successMessage", "관리자 계정이 삭제(비활성화)되었습니다.");
        } catch (Exception e) {
            log.warn("Failed to delete admin", e);
            redirectAttributes.addFlashAttribute("errorMessage", "관리자 삭제에 실패했습니다.");
        }
        return "redirect:/owner/admin/list";
    }

    @PostMapping("/{user-id}/restore")
    public String restoreAdmin(@PathVariable("user-id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            ownerService.restoreAdmin(userId);
            redirectAttributes.addFlashAttribute("successMessage", "관리자 계정이 복구되었습니다.");
        } catch (Exception e) {
            log.warn("Failed to restore admin", e);
            redirectAttributes.addFlashAttribute("errorMessage", "관리자 복구에 실패했습니다.");
        }
        return "redirect:/owner/admin/list";
    }

    @PostMapping("/{user-id}/reset-password")
    public String resetPassword(@PathVariable("user-id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            ownerService.resetPassword(userId);
            redirectAttributes.addFlashAttribute("successMessage", "관리자의 비밀번호가 1234 초기화되었습니다.");
        } catch (Exception e) {
            log.warn("Failed to reset password", e);
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 초기화에 실패했습니다.");
        }
        return "redirect:/owner/admin/list";
    }
}
