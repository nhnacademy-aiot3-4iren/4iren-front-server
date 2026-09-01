package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.*;
import com.nhnacademy.front.processing.dto.sensor.SensorSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BuildingController {

    private final TeamService teamService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final FrontSensorService frontSensorService;
    private final DeviceService deviceService;

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

        return "team/buildings";
    }

    @GetMapping("/teams/{teamId}/buildings/{buildingId}")
    public String buildingDetailPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            Model model
    ) {
        TeamDetailResponse team = teamService.getTeam(teamId);
        BuildingDetailResponse building = buildingService.getBuilding(teamId, buildingId);

        // 1. 해당 건물의 하위 강의실 목록 조회
        List<RoomDetailResponse> rooms = Collections.emptyList();
        try {
            PageResponse<RoomDetailResponse> roomPage = roomService.getRooms(teamId, buildingId, 0, 100, "id,ASC");
            if (roomPage != null && roomPage.content() != null) {
                rooms = roomPage.content();
            }
        } catch (Exception ignored) {}

        // 2. 해당 건물에 등록된 전체 센서 목록 조회
        List<SensorSummaryResponse> sensors = Collections.emptyList();
        try {
            sensors = frontSensorService.getSensorsByBuilding(buildingId);
        } catch (Exception ignored) {}

        // 3. 건물 내 각 강의실에 소속된 디바이스 목록 수집 ([강의실명] 디바이스명 매핑)
        List<BuildingDeviceSummary> buildingDevices = new ArrayList<>();
        for (RoomDetailResponse room : rooms) {
            try {
                PageResponse<DeviceResponse> devicePage = deviceService.getDevices(teamId, room.roomId(), 0, 100, "id,ASC");
                if (devicePage != null && devicePage.content() != null) {
                    for (DeviceResponse dev : devicePage.content()) {
                        buildingDevices.add(new BuildingDeviceSummary(
                                room.roomId(),
                                room.roomName(),
                                dev.deviceId(),
                                dev.deviceName()
                        ));
                    }
                }
            } catch (Exception ignored) {}
        }

        model.addAttribute("team", team);
        model.addAttribute("building", building);
        model.addAttribute("rooms", rooms);
        model.addAttribute("sensors", sensors);
        model.addAttribute("buildingDevices", buildingDevices);

        return "team/building-info";
    }

    // 화면 렌더링 전용 DTO 레코드
    public record BuildingDeviceSummary(
            Long roomId,
            String roomName,
            Long deviceId,
            String deviceName
    ) {}
}