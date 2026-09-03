package com.nhnacademy.front.dashboard.controller;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/dashboard/stream")
public class DashboardMetricStreamController {

    private final SensorMetricSseProxyService sensorMetricSseProxyService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamDashboardMetrics(
            @PathVariable @Positive Long teamId,
            @RequestParam(name = "roomId")
            @Size(min = 1, max = 50) List<@Positive Long> roomIds,
            @RequestParam(name = "metricCode")
            @Size(min = 1, max = 4) List<@NotBlank @Size(max = 50) String> metricCodes,
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        StreamingResponseBody stream = sensorMetricSseProxyService.openDashboardMetricStream(
                teamId,
                roomIds,
                metricCodes,
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
