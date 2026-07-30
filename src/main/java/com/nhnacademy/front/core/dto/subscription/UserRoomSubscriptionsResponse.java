package com.nhnacademy.front.core.dto.subscription;

import java.util.List;

public record UserRoomSubscriptionsResponse(
        Long userId,
        List<RoomSubInfo> roomSubInfo
) {
    public record RoomSubInfo(
            Long roomId,
            String roomName,
            boolean notificationEnabled
    ) {
    }
}
