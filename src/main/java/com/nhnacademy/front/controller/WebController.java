package com.nhnacademy.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/")
    public String getHome() {
        return "start";
    }

    @GetMapping("/forgot")
    public String getForgot() { return "account/forgot"; }

    @GetMapping("/flowdiy")
    public String getFlowdiy() { return "flow-diy"; }

    @GetMapping("/table")
    public String getAlertHis() { return "basic/table"; }

    @GetMapping("/team")
    public String getMyTeams() { return "team/myteams"; }

    @GetMapping("/alert-history")
    public String getAlertHistory() { return "alarms/alert-history"; }


}
