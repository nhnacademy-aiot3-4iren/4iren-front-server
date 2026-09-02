package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.sensor.metric.SensorMetricSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreSensorMetricClient",
        path = "/api/core/teams"
)
public interface CoreSensorMetricClient {

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-metrics/summary")
    SensorMetricSummaryResponse getSummary(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );
}
