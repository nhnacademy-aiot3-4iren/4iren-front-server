package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationCreateRequest;
import com.nhnacademy.front.core.dto.sensor.SensorLocationResponse;
import com.nhnacademy.front.core.service.SensorLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/rooms/{roomId}/sensor-locations")
public class CoreSensorLocationRestController {

    private final SensorLocationService sensorLocationService;

    @GetMapping
    public ResponseEntity<PageResponse<SensorLocationResponse>> getSensorLocations(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(sensorLocationService.getSensorLocations(teamId, roomId, page, size, sort));
    }

    @PostMapping
    public ResponseEntity<SensorLocationResponse> createSensorLocation(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @Valid @RequestBody SensorLocationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sensorLocationService.createSensorLocation(teamId, roomId, request));
    }

    @DeleteMapping("/{sensorLocationId}")
    public ResponseEntity<Void> deleteSensorLocation(
            @PathVariable Long teamId,
            @PathVariable Long sensorLocationId
    ) {
        sensorLocationService.deleteSensorLocation(teamId, sensorLocationId);
        return ResponseEntity.noContent().build();
    }
}
