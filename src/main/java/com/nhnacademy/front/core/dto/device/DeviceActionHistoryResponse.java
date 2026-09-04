package com.nhnacademy.front.core.dto.device;

import java.time.LocalDateTime;

public record DeviceActionHistoryResponse(
        Long historyId,
        Long deviceId,
        Long roomId,
        Long buildingId,
        String deviceName,
        DeviceAction action,
        LocalDateTime recordedAt,
        Weekday dayOfWeek
) {
}
