package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreSensorLocationClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationCreateRequest;
import com.nhnacademy.front.core.dto.sensor.SensorLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SensorLocationService {

    private final CoreSensorLocationClient coreSensorLocationClient;

    public PageResponse<SensorLocationResponse> getSensorLocations(
            Long teamId,
            Long roomId,
            Integer page,
            Integer size,
            String sort
    ) {
        return coreSensorLocationClient.getSensorLocations(teamId, roomId, page, size, sort);
    }

    public SensorLocationResponse createSensorLocation(
            Long teamId,
            Long roomId,
            SensorLocationCreateRequest request
    ) {
        return coreSensorLocationClient.createSensorLocation(teamId, roomId, request);
    }
}
