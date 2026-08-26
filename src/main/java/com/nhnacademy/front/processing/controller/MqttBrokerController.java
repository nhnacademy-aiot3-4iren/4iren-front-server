package com.nhnacademy.front.processing.controller;

import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.BuildingService;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.processing.dto.MqttBrokerCreateRequest;
import com.nhnacademy.front.processing.dto.MqttBrokerInfoDto;
import com.nhnacademy.front.processing.service.MqttBrokerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MqttBrokerController {

    private final TeamService teamService;
    private final BuildingService buildingService;

    @GetMapping("/teams/{teamId}/buildings/{buildingId}/mqtt")
    public String mqttRegisterPage(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            Model model
    ) {
        TeamDetailResponse team = teamService.getTeam(teamId);
        BuildingDetailResponse building = buildingService.getBuilding(teamId, buildingId);

        model.addAttribute("team", team);
        model.addAttribute("building", building);

        return "mypage/mqtt-register";
    }

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/front/teams/{teamId}/buildings/{buildingId}/mqtt")
    static class MqttBrokerRestController {

        private final MqttBrokerService mqttBrokerService;

        @PostMapping
        public ResponseEntity<MqttBrokerInfoDto> registerBroker(
                @PathVariable Long teamId,
                @PathVariable Long buildingId,
                @Valid @RequestBody MqttBrokerCreateRequest request
        ) {
            return ResponseEntity.status(HttpStatus.CREATED).body(mqttBrokerService.registerBroker(request));
        }
    }
}
