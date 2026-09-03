package com.nhnacademy.front.rule.dto.flow;

import java.util.List;

public record FlowListResponse (
        List<FlowResponse> flowResponseList

){
    record FlowResponse(
            Long flowId,

            String flowName,

            String description,

            boolean isActive,

            Long scheduleCount,

            String createdAt,


            String updatedAt
    ) {}
}
