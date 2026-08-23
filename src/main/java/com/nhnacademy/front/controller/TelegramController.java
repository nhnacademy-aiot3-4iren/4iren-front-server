package com.nhnacademy.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
public class TelegramController {

    @GetMapping("/tele")
    public String getTelegram(
            @RequestHeader(name = "X-USER-ROLE", required = false) String role,
            Model model
    ) {
        String debugId = java.util.UUID.randomUUID().toString().substring(0, 8);
        System.out.println("[DEBUG-" + debugId + "] role=[" + role + "]");
        model.addAttribute("debugId", debugId);  // 임시로 화면에도 찍어보기

        boolean isAdmin = "ADMIN".equals(role);
        model.addAttribute("isAdmin", isAdmin);
        return "telegram/telegram";
    }


}