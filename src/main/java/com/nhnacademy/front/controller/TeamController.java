package com.nhnacademy.front.controller;

import com.nhnacademy.front.core.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/team-info/{teamId}")
    public String teamInfoPage(@PathVariable Long teamId, Model model) {
        model.addAttribute("team", teamService.getTeam(teamId));
        return "team/admin/team-info";
    }

    @GetMapping("/team-info")
    public String teamInfoPageWithoutId() {
        return "redirect:/team";
    }
}
