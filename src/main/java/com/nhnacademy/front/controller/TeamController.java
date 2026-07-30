package com.nhnacademy.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TeamController {

    @GetMapping("/team-info")
    public String teamInfoPage() {
        return "team-info";
    }
}
