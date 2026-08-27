package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.*;
import com.nhnacademy.front.recommendation.service.RoomBriefingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RoomAssetController {

    private final TeamService teamService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final DeviceService deviceService;
    private final SensorLocationService sensorLocationService;
    private final RoomBriefingService roomBriefingService;

    @GetMapping("/teams/{teamId}/buildings/{buildingId}/rooms/{roomId}")
    public String roomDetailPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @PathVariable Long roomId,
            Model model
    ) {
        addRoomContext(teamId, buildingId, roomId, model);
        model.addAttribute("subscriptionStatus", roomService.getSubscriptionStatus(teamId, roomId));
        roomBriefingService.getWelcomeBriefing(teamId, roomId)
                .ifPresent(briefing -> model.addAttribute("welcomeBriefing", briefing));
        roomBriefingService.getDailySummary(teamId, roomId)
                .ifPresent(summary -> model.addAttribute("dailySummary", summary));
        return "mypage/room-info";
    }

    @GetMapping("/teams/{teamId}/buildings/{buildingId}/rooms/{roomId}/devices")
    public String deviceListPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @PathVariable Long roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort,
            Model model
    ) {
        addRoomContext(teamId, buildingId, roomId, model);
        PageResponse<DeviceResponse> devices = deviceService.getDevices(teamId, roomId, page, size, sort);

        model.addAttribute("devices", devices);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "mypage/devices";
    }

    @GetMapping("/teams/{teamId}/buildings/{buildingId}/rooms/{roomId}/sensor-locations")
    public String sensorLocationListPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @PathVariable Long roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort,
            Model model
    ) {
        addRoomContext(teamId, buildingId, roomId, model);
        PageResponse<SensorLocationResponse> sensorLocations =
                sensorLocationService.getSensorLocations(teamId, roomId, page, size, sort);

        model.addAttribute("sensorLocations", sensorLocations);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "mypage/sensor-locations";
    }

    private TeamDetailResponse addRoomContext(Long teamId, Long buildingId, Long roomId, Model model) {
        TeamDetailResponse team = teamService.getTeam(teamId);
        BuildingDetailResponse building = buildingService.getBuilding(teamId, buildingId);
        RoomDetailResponse room = roomService.getRoom(teamId, roomId);

        model.addAttribute("team", team);
        model.addAttribute("building", building);
        model.addAttribute("room", room);

        return team;
    }
}
