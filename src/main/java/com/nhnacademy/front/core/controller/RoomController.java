package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionStatus;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.BuildingService;
import com.nhnacademy.front.core.service.RoomService;
import com.nhnacademy.front.core.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final TeamService teamService;
    private final BuildingService buildingService;
    private final RoomService roomService;

    @GetMapping("/teams/{teamId}/buildings/{buildingId}/rooms")
    public String roomListPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort,
            Model model
    ) {
        TeamDetailResponse team = teamService.getTeam(teamId);
        BuildingDetailResponse building = buildingService.getBuilding(teamId, buildingId);
        PageResponse<RoomDetailResponse> rooms = roomService.getRooms(teamId, buildingId, page, size, sort);
        Map<Long, RoomSubscriptionStatus> roomSubscriptionStatuses = roomService.getSubscriptionStatuses(
                teamId,
                rooms.content().stream()
                        .map(RoomDetailResponse::roomId)
                        .toList()
        );

        model.addAttribute("team", team);
        model.addAttribute("building", building);
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomSubscriptionStatuses", roomSubscriptionStatuses);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        // 파일 위치: templates/sidebar-menu/team/classrooms/rooms.html
        return "team/rooms";
    }
}