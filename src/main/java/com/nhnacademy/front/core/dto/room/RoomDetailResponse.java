package com.nhnacademy.front.core.dto.room;

public record RoomDetailResponse(
        Long roomId,
        Long buildingId,
        String buildingName,
        String roomName,
        String description,
        long sensorCount,
        long deviceCount
) {
}
