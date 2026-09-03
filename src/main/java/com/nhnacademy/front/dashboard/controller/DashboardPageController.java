package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardPageController {

    private final TeamService teamService;

    @GetMapping("/dashboard")
    public String dashboardEntry(Model model) {
        List<TeamResponse> teams = teamService.getAllTeams();
        if (teams.isEmpty()) {
            model.addAttribute("teams", teams);
            model.addAttribute("currentTeam", null);
            return "dashboard/dashboard-list";
        }
        return "redirect:/teams/" + teams.getFirst().teamId() + "/dashboard";
    }

    @GetMapping("/teams/{teamId}/dashboard")
    public String dashboardPage(@PathVariable Long teamId, Model model) {
        List<TeamResponse> teams = teamService.getAllTeams();
        TeamResponse currentTeam = teams.stream()
                .filter(team -> teamId.equals(team.teamId()))
                .findFirst()
                .orElseGet(() -> {
                    var detail = teamService.getTeam(teamId);
                    return new TeamResponse(
                            detail.teamId(),
                            detail.teamName(),
                            detail.description(),
                            detail.status(),
                            detail.statusCause(),
                            detail.statusChangedAt(),
                            detail.myRole()
                    );
                });

        model.addAttribute("teams", teams);
        model.addAttribute("currentTeam", currentTeam);
        model.addAttribute("currentTeamId", teamId);
        return "dashboard/dashboard-list";
    }
}
