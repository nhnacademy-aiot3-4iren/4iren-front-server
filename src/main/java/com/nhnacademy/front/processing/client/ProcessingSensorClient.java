package com.nhnacademy.front.processing.client;

import com.nhnacademy.front.processing.dto.sensor.SensorRoomAssignmentRequest;
import com.nhnacademy.front.processing.dto.sensor.SensorSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "4iren-gateway", contextId = "processingSensorClient", path = "/api/processing/sensors")
public interface ProcessingSensorClient {

    @GetMapping("/buildings/{buildingId}")
    List<SensorSummaryResponse> getSensorsByBuilding(@PathVariable Long buildingId);

    @PatchMapping("/rooms")
    void assignRooms(@RequestBody List<SensorRoomAssignmentRequest> requests);
}
