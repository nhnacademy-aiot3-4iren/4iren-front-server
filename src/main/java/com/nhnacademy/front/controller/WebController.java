package com.nhnacademy.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/")
    public String getHome() {
        return "/mypage-setting";
    }

//    @GetMapping("/llm")
//    public String getLlm() {
//        return "llm";
//    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSign() { return "signup"; }

    @GetMapping("/forgot")
    public String getForgot() { return "forgot"; }

    @GetMapping("/flowdiy")
    public String getFlowdiy() { return "flow-diy"; }

    @GetMapping("/mypage")
    public String getMypage() { return "mypage"; }

    @GetMapping("/telegram")
    public String getTelegram() { return "setting-telegram"; }

}
