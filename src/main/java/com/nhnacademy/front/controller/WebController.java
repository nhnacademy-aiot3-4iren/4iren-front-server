package com.nhnacademy.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/")
    public String getHome() {
        return "/start";
    }

//    @GetMapping("/llm")
//    public String getLlm() {
//        return "llm";
//    }

    @GetMapping("/login")
    public String getLogin() {
        return "/account/login";
    }

    @GetMapping("/signup")
    public String getSign() { return "/account/signup"; }

    @GetMapping("/forgot")
    public String getForgot() { return "/account/forgot"; }

    @GetMapping("/flowdiy")
    public String getFlowdiy() { return "flow-diy"; }

    @GetMapping("/mypage")
    public String getMypage() { return "/mypage/mypage"; }

    @GetMapping("/table")
    public String getAlertHis() { return "basic/table"; }


}
