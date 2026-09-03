package com.nhnacademy.front.rule.dto.flowschedule;

import java.util.List;

public record FlowScheduleListResponse(
        Long flowId,
        List<FlowScheduleResponse> schedules
) {
}
