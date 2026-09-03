package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreSensorMetricClient;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricCatalogResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricSeriesResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomMetricSummaryResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomSensorMetricLatestResponse;
import com.nhnacademy.front.core.dto.sensor.metric.RoomSensorMetricSeriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorMetricService {

    private final CoreSensorMetricClient coreSensorMetricClient;

    public RoomMetricCatalogResponse getRoomMetricCatalog(Long teamId, Long roomId) {
        return coreSensorMetricClient.getRoomMetricCatalog(teamId, roomId);
    }

    public RoomMetricSummaryResponse getRoomMetricSummary(Long teamId, Long roomId) {
        return coreSensorMetricClient.getRoomMetricSummary(teamId, roomId);
    }

    public RoomSensorMetricLatestResponse getLatestRoomSensorMetrics(Long teamId, Long roomId) {
        return coreSensorMetricClient.getLatestRoomSensorMetrics(teamId, roomId);
    }

    public RoomMetricSeriesResponse getRoomMetricSeries(
            Long teamId,
            Long roomId,
            String metricCode,
            Instant from,
            Instant to,
            Duration interval
    ) {
        return coreSensorMetricClient.getRoomMetricSeries(
                teamId,
                roomId,
                metricCode,
                from,
                to,
                interval
        );
    }

    public RoomSensorMetricSeriesResponse getRoomSensorMetricSeries(
            Long teamId,
            Long roomId,
            Instant from,
            Instant to,
            Duration interval,
            List<String> devEuis,
            List<String> metricCodes
    ) {
        return coreSensorMetricClient.getRoomSensorMetricSeries(
                teamId,
                roomId,
                from,
                to,
                interval,
                devEuis,
                metricCodes
        );
    }
}
