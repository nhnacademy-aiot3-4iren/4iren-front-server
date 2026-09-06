package com.nhnacademy.front.rule.dto.flowschedule;

import jakarta.validation.constraints.NotNull;

public record FlowScheduleCreateRequest(
        @NotNull
        String dayOfWeek,

        @NotNull
        String startTime,

        @NotNull
        String endTime
) {
}
