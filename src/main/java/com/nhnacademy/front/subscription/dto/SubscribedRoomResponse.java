package com.nhnacademy.front.subscription.dto;

public record SubscribedRoomResponse(
        Long teamId,
        Long buildingId,
        Long roomId,
        String buildingName,
        String roomName
) {
}
