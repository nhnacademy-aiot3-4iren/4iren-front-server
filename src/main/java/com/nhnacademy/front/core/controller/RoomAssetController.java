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

import java.util.Collections;
import java.util.List;

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

        // 1. 해당 강의실에 등록된 센서 목록 조회 (최대 100개)
        List<SensorLocationResponse> sensorLocations = Collections.emptyList();
        try {
            PageResponse<SensorLocationResponse> sensorPage =
                    sensorLocationService.getSensorLocations(teamId, roomId, 0, 100, "id,ASC");
            if (sensorPage != null && sensorPage.content() != null) {
                sensorLocations = sensorPage.content();
            }
        } catch (Exception ignored) {}

        // 2. 해당 강의실에 등록된 디바이스 목록 조회 (최대 100개)
        List<DeviceResponse> devices = Collections.emptyList();
        try {
            PageResponse<DeviceResponse> devicePage =
                    deviceService.getDevices(teamId, roomId, 0, 100, "id,ASC");
            if (devicePage != null && devicePage.content() != null) {
                devices = devicePage.content();
            }
        } catch (Exception ignored) {}

        model.addAttribute("sensorLocations", sensorLocations);
        model.addAttribute("devices", devices);

        return "team/room-info";
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
