package com.nhnacademy.front.core.dto.room;

public record RoomResponse(
        Long roomId,
        Long buildingId,
        String roomName,
        String description
) {
}
