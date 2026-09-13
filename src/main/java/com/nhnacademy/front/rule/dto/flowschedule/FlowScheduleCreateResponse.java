package com.nhnacademy.front.rule.dto.flowschedule;

import java.util.List;

public record FlowScheduleCreateResponse(
        List<Long> scheduleIds
) {
}
