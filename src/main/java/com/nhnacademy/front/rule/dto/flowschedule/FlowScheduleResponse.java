package com.nhnacademy.front.rule.dto.flowschedule;

public record FlowScheduleResponse(
        Long scheduleId,

        String dayOfWeek,

        String startTime,

        String endTime
) {
}
