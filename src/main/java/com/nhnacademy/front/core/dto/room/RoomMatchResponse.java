package com.nhnacademy.front.core.dto.room;

public record RoomMatchResponse(
        Long roomId,
        Long buildingId,
        String buildingName,
        String roomName
) {
}
