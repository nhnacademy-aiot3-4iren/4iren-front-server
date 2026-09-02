package com.nhnacademy.front.dashboard.controller;

import com.nhnacademy.front.core.service.SensorMetricService;
import com.nhnacademy.front.dashboard.dto.DashboardMetricSummaryResponse;
import com.nhnacademy.front.dashboard.dto.DashboardRoomResponse;
import com.nhnacademy.front.dashboard.service.DashboardRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/dashboard")
public class DashboardRestController {

    private final SensorMetricService sensorMetricService;
    private final DashboardRoomService dashboardRoomService;

    @GetMapping("/rooms")
    public List<DashboardRoomResponse> getDashboardRooms(@ModelAttribute("userId") Long userId) {
        return dashboardRoomService.getSubscribedRooms(userId);
    }

    @GetMapping("/teams/{teamId}/rooms/{roomId}/summary")
    public DashboardMetricSummaryResponse getSummary(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        return DashboardMetricSummaryResponse.from(sensorMetricService.getSummary(teamId, roomId));
    }
}
