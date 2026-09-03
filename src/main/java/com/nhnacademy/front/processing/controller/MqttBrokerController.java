package com.nhnacademy.front.processing.controller;

import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerInfoDto;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerRegisterRequest;
import com.nhnacademy.front.processing.service.MqttBrokerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/buildings/{buildingId}/mqtt")
public class MqttBrokerController {

    private final MqttBrokerService mqttBrokerService;

    // 1. 빌딩의 현재 MQTT 설정값 조회 (모달 열릴 때 자동 채우기)
    @GetMapping
    public ResponseEntity<MqttBrokerInfoDto> getBroker(@PathVariable Long teamId,
                                                       @PathVariable Long buildingId) {
        return ResponseEntity.ok(mqttBrokerService.getBrokerByBuilding(buildingId));
    }

    // 2. 빌딩 MQTT 설정 등록
    @PostMapping
    public ResponseEntity<MqttBrokerInfoDto> registerBroker(@PathVariable Long teamId,
                                                            @PathVariable Long buildingId,
                                                            @Valid @RequestBody MqttBrokerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mqttBrokerService.registerBroker(buildingId, request));
    }

    // 3. 빌딩 MQTT 설정 수정
    @PutMapping
    public ResponseEntity<MqttBrokerInfoDto> updateBroker(@PathVariable Long teamId,
                                                          @PathVariable Long buildingId,
                                                          @Valid @RequestBody MqttBrokerRegisterRequest request) {
        return ResponseEntity.ok(mqttBrokerService.updateBroker(buildingId, request));
    }
}
