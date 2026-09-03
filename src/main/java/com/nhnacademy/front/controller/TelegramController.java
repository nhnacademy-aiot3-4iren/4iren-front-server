package com.nhnacademy.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TelegramController {

    @GetMapping("/tele")
    public String getTelegram(@ModelAttribute("role") String role, Model model) {
        log.debug("[TelegramController] user Role: {}", role);
        boolean flag = role.equals("ADMIN") || role.equals("OWNER");
        model.addAttribute("isAdmin", flag);
        return "telegram/telegram";
    }

}