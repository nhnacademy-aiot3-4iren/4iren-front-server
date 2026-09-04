package com.nhnacademy.front.core.dto.device;

import jakarta.validation.constraints.NotNull;

public record DeviceActionRequest(
        @NotNull(message = "기기 동작은 null일 수 없습니다.")
        DeviceAction action
) {
}
