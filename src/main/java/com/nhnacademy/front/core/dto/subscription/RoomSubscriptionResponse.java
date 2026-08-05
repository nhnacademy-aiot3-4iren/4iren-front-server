package com.nhnacademy.front.core.dto.subscription;

public record RoomSubscriptionResponse(
        Long roomSubscriptionId,
        Long roomId,
        boolean notificationEnabled
) {
}
