package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationCreateRequest;
import com.nhnacademy.front.core.dto.sensor.SensorLocationResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreSensorLocationClient",
        path = "/api/core/teams"
)
public interface CoreSensorLocationClient {

    @PostMapping("/{teamId}/rooms/{roomId}/sensor-locations")
    SensorLocationResponse createSensorLocation(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestBody SensorLocationCreateRequest request
    );

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-locations")
    PageResponse<SensorLocationResponse> getSensorLocations(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-locations/all")
    List<SensorLocationResponse> getAllSensorLocations(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/{teamId}/sensor-locations/{sensorLocationId}")
    SensorLocationResponse getSensorLocation(
            @PathVariable("teamId") Long teamId,
            @PathVariable("sensorLocationId") Long sensorLocationId
    );

    @PatchMapping("/{teamId}/sensor-locations/{sensorLocationId}")
    SensorLocationResponse updateSensorLocation(
            @PathVariable("teamId") Long teamId,
            @PathVariable("sensorLocationId") Long sensorLocationId,
            @RequestBody SensorLocationUpdateRequest request
    );

    @DeleteMapping("/{teamId}/sensor-locations/{sensorLocationId}")
    void deleteSensorLocation(
            @PathVariable("teamId") Long teamId,
            @PathVariable("sensorLocationId") Long sensorLocationId
    );
}
