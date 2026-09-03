package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationCreateRequest;
import com.nhnacademy.front.core.dto.sensor.SensorLocationResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationUpdateRequest;
import com.nhnacademy.front.core.service.SensorLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<List<SensorLocationResponse>> getAllSensorLocations(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(sensorLocationService.getAllSensorLocations(teamId, roomId));
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

    @GetMapping("/{sensorLocationId}")
    public ResponseEntity<SensorLocationResponse> getSensorLocation(
            @PathVariable Long teamId,
            @PathVariable Long sensorLocationId
    ) {
        return ResponseEntity.ok(sensorLocationService.getSensorLocation(teamId, sensorLocationId));
    }

    @PatchMapping("/{sensorLocationId}")
    public ResponseEntity<SensorLocationResponse> updateSensorLocation(
            @PathVariable Long teamId,
            @PathVariable Long sensorLocationId,
            @Valid @RequestBody SensorLocationUpdateRequest request
    ) {
        return ResponseEntity.ok(sensorLocationService.updateSensorLocation(teamId, sensorLocationId, request));
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
