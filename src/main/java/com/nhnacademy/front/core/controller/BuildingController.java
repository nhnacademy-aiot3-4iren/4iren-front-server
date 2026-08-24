package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.BuildingService;
import com.nhnacademy.front.core.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BuildingController {

    private final TeamService teamService;
    private final BuildingService buildingService;

    @GetMapping("/teams/{teamId}/buildings")
    public String buildingListPage(
            @PathVariable Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort,
            Model model
    ) {
        TeamDetailResponse team = teamService.getTeam(teamId);
        PageResponse<BuildingDetailResponse> buildings = buildingService.getBuildings(teamId, page, size, sort);

        model.addAttribute("team", team);
        model.addAttribute("buildings", buildings);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "mypage/buildings";
    }

    @GetMapping("/teams/{teamId}/buildings/{buildingId}")
    public String buildingDetailPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            Model model
    ) {
        TeamDetailResponse team = teamService.getTeam(teamId);
        BuildingDetailResponse building = buildingService.getBuilding(teamId, buildingId);

        model.addAttribute("team", team);
        model.addAttribute("building", building);

        return "mypage/building-info";
    }
}
