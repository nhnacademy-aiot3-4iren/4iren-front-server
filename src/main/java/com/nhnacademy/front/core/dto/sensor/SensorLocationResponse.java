package com.nhnacademy.front.core.dto.sensor;

public record SensorLocationResponse(
        Long sensorLocationId,
        Long roomId,
        String devEui,
        String locationDetail
) {
}
