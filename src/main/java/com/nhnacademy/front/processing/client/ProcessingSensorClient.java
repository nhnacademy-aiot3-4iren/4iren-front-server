package com.nhnacademy.front.processing.client;

import com.nhnacademy.front.processing.dto.sensor.SensorRoomAssignmentRequest;
import com.nhnacademy.front.processing.dto.sensor.SensorSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "4iren-gateway", contextId = "processingSensorClient", path = "/api/processing")
public interface ProcessingSensorClient {

    @GetMapping("/sensors/buildings/{buildingId}")
    List<SensorSummaryResponse> getSensorsByBuilding(@PathVariable("buildingId") Long buildingId);

    @GetMapping("/sensors/buildings/{buildingId}/unassigned")
    List<SensorSummaryResponse> getUnassignedSensorsByBuilding(@PathVariable("buildingId") Long buildingId);

    @PatchMapping("/sensors/rooms")
    void assignRooms(@RequestBody List<SensorRoomAssignmentRequest> requests);

    @DeleteMapping("/rooms/{roomId}/sensors")
    void unassignRoom(@PathVariable("roomId") Integer roomId);
}