package com.nhnacademy.front.core.dto.subscription;

public record RoomSubscriptionStatus(
        boolean subscribed,
        boolean notificationEnabled
) {
    public static RoomSubscriptionStatus unsubscribed() {
        return new RoomSubscriptionStatus(false, false);
    }
}
