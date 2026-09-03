package com.nhnacademy.front.core.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreSensorMetricStreamClient",
        path = "/api/core/teams"
)
public interface CoreSensorMetricStreamClient {

    /**
     * SSE 응답을 DTO로 디코딩하지 않고 열린 응답 스트림 그대로 반환한다.
     * 호출자는 반드시 {@link Response#close()}로 응답을 닫아야 한다.
     */
    @GetMapping(
            value = "/{teamId}/rooms/{roomId}/sensor-metrics/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    Response openRoomSensorMetricStream(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestParam(name = "devEui", required = false) List<String> devEuis,
            @RequestParam(name = "metricCode", required = false) List<String> metricCodes,
            @RequestParam(name = "since", required = false) Instant since,
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId
    );

    @GetMapping(
            value = "/{teamId}/dashboard/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    Response openDashboardMetricStream(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "roomId") List<Long> roomIds,
            @RequestParam(name = "metricCode") List<String> metricCodes,
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId
    );
}
