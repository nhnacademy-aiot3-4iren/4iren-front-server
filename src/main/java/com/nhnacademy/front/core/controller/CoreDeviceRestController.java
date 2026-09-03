package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @Valid @RequestBody DeviceCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(teamId, roomId, request));
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
