package com.nhnacademy.front.rule.dto.flowschedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleCreateRequest(
        @NotNull
        DayOfWeek dayOfWeek,

        @NotNull
        String startTime,

        @NotNull
        String endTime
) {
}
