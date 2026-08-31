package com.nhnacademy.front.processing.dto.sensor;

public record SensorSummaryResponse(
        String devEui,
        Long buildingId,
        String deviceName,
        String location,
        String point
) {}
