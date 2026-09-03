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

    // 프론트엔드의 "센서 추가" 모달에서 호출 시, 미배정 센서만 조회되도록 변경
    @GetMapping("/sensors")
    public ResponseEntity<List<SensorSummaryResponse>> getSensorsByBuilding(
            @PathVariable Long teamId,
            @PathVariable Long buildingId
    ) {
        return ResponseEntity.ok(frontSensorService.getUnassignedSensorsByBuilding(buildingId));
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