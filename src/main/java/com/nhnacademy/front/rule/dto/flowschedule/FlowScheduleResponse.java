package com.nhnacademy.front.rule.dto.flowschedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleResponse(
        Long scheduleId,

        DayOfWeek dayOfWeek,

        String startTime,

        String endTime
) {
}
