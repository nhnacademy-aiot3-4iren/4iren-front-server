package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricCatalogResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricSeriesResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricSummaryResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomSensorMetricLatestResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomSensorMetricSeriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreSensorMetricClient",
        path = "/api/core/teams"
)
public interface CoreSensorMetricClient {

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-metrics/catalog")
    RoomMetricCatalogResponse getRoomMetricCatalog(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-metrics/summary")
    RoomMetricSummaryResponse getRoomMetricSummary(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-metrics/latest")
    RoomSensorMetricLatestResponse getLatestRoomSensorMetrics(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-metrics/series")
    RoomMetricSeriesResponse getRoomMetricSeries(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestParam("metricCode") String metricCode,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("interval") Duration interval
    );

    @GetMapping("/{teamId}/rooms/{roomId}/sensor-metrics/sensors/series")
    RoomSensorMetricSeriesResponse getRoomSensorMetricSeries(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam("interval") Duration interval,
            @RequestParam(name = "devEui", required = false) List<String> devEuis,
            @RequestParam(name = "metricCode", required = false) List<String> metricCodes
    );
}
