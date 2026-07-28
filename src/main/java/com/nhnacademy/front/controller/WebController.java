package com.nhnacademy.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/")
    public String getLlm() {
        return "flow-diy";
    }

    @GetMapping("/home")
    public String getHome() {
        return "start";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSign() { return "signup"; }

    @GetMapping("/forgot")
    public String getForgot() { return "forgot"; }
}
