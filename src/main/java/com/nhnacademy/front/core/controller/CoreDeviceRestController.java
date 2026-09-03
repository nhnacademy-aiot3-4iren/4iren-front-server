package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DevicePowerStateUpdateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.device.DeviceUpdateRequest;
import com.nhnacademy.front.core.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/rooms/{roomId}/devices")
public class CoreDeviceRestController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<PageResponse<DeviceResponse>> getDevices(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(deviceService.getDevices(teamId, roomId, page, size, sort));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DeviceResponse>> getAllDevices(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(deviceService.getAllDevices(teamId, roomId));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @Valid @RequestBody DeviceCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(teamId, roomId, request));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> getDevice(
            @PathVariable Long teamId,
            @PathVariable Long deviceId
    ) {
        return ResponseEntity.ok(deviceService.getDevice(teamId, deviceId));
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable Long teamId,
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceUpdateRequest request
    ) {
        return ResponseEntity.ok(deviceService.updateDevice(teamId, deviceId, request));
    }

    @PatchMapping("/{deviceId}/power-state")
    public ResponseEntity<DeviceResponse> updateDevicePowerState(
            @PathVariable Long teamId,
            @PathVariable Long deviceId,
            @Valid @RequestBody DevicePowerStateUpdateRequest request
    ) {
        return ResponseEntity.ok(deviceService.updateDevicePowerState(teamId, deviceId, request));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Long teamId,
            @PathVariable Long deviceId
    ) {
        deviceService.deleteDevice(teamId, deviceId);
        return ResponseEntity.noContent().build();
    }
}
