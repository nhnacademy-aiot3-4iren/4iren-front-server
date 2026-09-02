package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreSensorMetricClient;
import com.nhnacademy.front.core.dto.sensor.metric.SensorMetricSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SensorMetricService {

    private final CoreSensorMetricClient coreSensorMetricClient;

    public SensorMetricSummaryResponse getSummary(Long teamId, Long roomId) {
        return coreSensorMetricClient.getSummary(teamId, roomId);
    }
}
