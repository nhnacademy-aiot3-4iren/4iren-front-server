package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricCatalogResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricSeriesResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricSummaryResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomSensorMetricLatestResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomSensorMetricSeriesResponse;
import com.nhnacademy.front.core.service.SensorMetricService;
import com.nhnacademy.front.core.service.SensorMetricSseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/rooms/{roomId}/sensor-metrics")
public class CoreSensorMetricRestController {

    private final SensorMetricService sensorMetricService;
    private final SensorMetricSseProxyService sensorMetricSseProxyService;

    @GetMapping("/catalog")
    public ResponseEntity<RoomMetricCatalogResponse> getRoomMetricCatalog(
            @PathVariable @Positive Long teamId,
            @PathVariable @Positive Long roomId
    ) {
        return ResponseEntity.ok(sensorMetricService.getRoomMetricCatalog(teamId, roomId));
    }

    @GetMapping("/summary")
    public ResponseEntity<RoomMetricSummaryResponse> getRoomMetricSummary(
            @PathVariable @Positive Long teamId,
            @PathVariable @Positive Long roomId
    ) {
        return ResponseEntity.ok(sensorMetricService.getRoomMetricSummary(teamId, roomId));
    }

    @GetMapping("/latest")
    public ResponseEntity<RoomSensorMetricLatestResponse> getLatestRoomSensorMetrics(
            @PathVariable @Positive Long teamId,
            @PathVariable @Positive Long roomId
    ) {
        return ResponseEntity.ok(sensorMetricService.getLatestRoomSensorMetrics(teamId, roomId));
    }

    @GetMapping("/series")
    public ResponseEntity<RoomMetricSeriesResponse> getRoomMetricSeries(
            @PathVariable @Positive Long teamId,
            @PathVariable @Positive Long roomId,
            @RequestParam @NotBlank @Size(max = 50) String metricCode,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam Duration interval
    ) {
        return ResponseEntity.ok(sensorMetricService.getRoomMetricSeries(
                teamId,
                roomId,
                metricCode,
                from,
                to,
                interval
        ));
    }

    @GetMapping("/sensors/series")
    public ResponseEntity<RoomSensorMetricSeriesResponse> getRoomSensorMetricSeries(
            @PathVariable @Positive Long teamId,
            @PathVariable @Positive Long roomId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam Duration interval,
            @RequestParam(name = "devEui", required = false) List<String> devEuis,
            @RequestParam(name = "metricCode", required = false) List<String> metricCodes
    ) {
        return ResponseEntity.ok(sensorMetricService.getRoomSensorMetricSeries(
                teamId,
                roomId,
                from,
                to,
                interval,
                devEuis,
                metricCodes
        ));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamRoomSensorMetrics(
            @PathVariable @Positive Long teamId,
            @PathVariable @Positive Long roomId,
            @RequestParam(name = "devEui", required = false) List<String> devEuis,
            @RequestParam(name = "metricCode", required = false) List<String> metricCodes,
            @RequestParam(name = "since", required = false) Instant since,
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        StreamingResponseBody stream = sensorMetricSseProxyService.openRoomSensorMetricStream(
                teamId,
                roomId,
                devEuis,
                metricCodes,
                since,
                lastEventId,
                request,
                response
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
                .header("X-Accel-Buffering", "no")
                .body(stream);
    }
}
