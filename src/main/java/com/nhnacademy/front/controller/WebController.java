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
    public String getForgot() {
        return "account/forgot";
    }

    @GetMapping("/flowdiy")
    public String getFlowdiy() {
        return "flow-diy";
    }

    @GetMapping("/table")
    public String getAlertHis() {
        // basic/table -> layout/table 로 변경
        return "layout/table";
    }

    @GetMapping("/team")
    public String getMyTeams() {
        // sidebar-menu/team/team-list -> team/team-list 로 뎁스 축소
        return "team/team-list";
    }

    @GetMapping("/alert-history")
    public String getAlertHistory() {
        return "alarms/alert-history";
    }

    @GetMapping("/settings")
    public String getSettings() {
        // sidebar-menu/settings/settings -> settings/settings 로 뎁스 축소
        return "settings/settings";
    }

}