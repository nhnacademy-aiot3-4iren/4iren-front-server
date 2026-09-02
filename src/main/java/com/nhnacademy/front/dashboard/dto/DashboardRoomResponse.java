package com.nhnacademy.front.dashboard.dto;

public record DashboardRoomResponse(
        Long teamId,
        Long roomId,
        Long buildingId,
        String buildingName,
        String roomName
) {
}
