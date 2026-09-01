package com.nhnacademy.front.processing.dto.sensor;

public record SensorRoomAssignmentRequest (
        String devEui,
        Long buildingId,
        Integer roomId
) {}
