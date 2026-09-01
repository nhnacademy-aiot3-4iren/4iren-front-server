package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.sensor.SensorBulkAssignRequest;
import com.nhnacademy.front.core.service.FrontSensorService;
import com.nhnacademy.front.processing.dto.sensor.SensorSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/buildings/{buildingId}")
public class FrontSensorIntegrationController {

    private final FrontSensorService frontSensorService;

    // 모달 창에서 빌딩에 속한 전체 센서 목록을 불러오기 위한 API
    @GetMapping("/sensors")
    public ResponseEntity<List<SensorSummaryResponse>> getSensorsByBuilding(
            @PathVariable Long teamId,
            @PathVariable Long buildingId
    ) {
        return ResponseEntity.ok(frontSensorService.getSensorsByBuilding(buildingId));
    }

    // 체크박스로 선택한 센서들을 룸에 일괄 등록(Core & Processing 이중 할당 연동)
    @PostMapping("/rooms/{roomId}/sensors/bulk")
    public ResponseEntity<Void> bulkAssignSensors(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @PathVariable Long roomId,
            @RequestBody SensorBulkAssignRequest request
    ) {
        frontSensorService.assignSensorsToRoom(teamId, buildingId, roomId, request.sensorIds());
        return ResponseEntity.ok().build();
    }
}