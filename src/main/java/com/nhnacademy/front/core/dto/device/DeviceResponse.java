package com.nhnacademy.front.core.dto.device;

public record DeviceResponse(
        Long deviceId,
        Long roomId,
        String deviceName,
        DeviceAction action
) {
}
