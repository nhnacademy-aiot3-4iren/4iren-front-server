package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.device.DeviceActionHistoryResponse;
import com.nhnacademy.front.core.dto.device.DeviceActionRequest;
import com.nhnacademy.front.core.dto.device.Weekday;
import com.nhnacademy.front.core.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}")
public class CoreDeviceActionHistoryRestController {

    private final DeviceService deviceService;

    @PostMapping("/devices/{deviceId}/action-histories")
    public ResponseEntity<Void> createDeviceActionHistory(
            @PathVariable Long teamId,
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceActionRequest request
    ) {
        deviceService.createDeviceActionHistory(teamId, deviceId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}/device-action-histories")
    public ResponseEntity<List<DeviceActionHistoryResponse>> getDeviceActionHistories(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @RequestParam(name = "deviceId", required = false) Long deviceId,
            @RequestParam(name = "dayOfWeek", required = false) Weekday dayOfWeek,
            @RequestParam(name = "startAt", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startAt,
            @RequestParam(name = "endAt", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endAt
    ) {
        return ResponseEntity.ok(
                deviceService.getDeviceActionHistories(teamId, roomId, deviceId, dayOfWeek, startAt, endAt)
        );
    }

    @GetMapping("/action-histories/{historyId}")
    public ResponseEntity<DeviceActionHistoryResponse> getDeviceActionHistory(
            @PathVariable Long teamId,
            @PathVariable Long historyId
    ) {
        return ResponseEntity.ok(deviceService.getDeviceActionHistory(teamId, historyId));
    }
}
