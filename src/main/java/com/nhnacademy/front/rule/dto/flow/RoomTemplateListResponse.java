package com.nhnacademy.front.rule.dto.flow;

import java.util.List;

public record RoomTemplateListResponse (
        List<RoomTemplateResponse> roomTemplateResponseList
){
    record RoomTemplateResponse(
            Long templateId,

            String templateName,

            String description,

            List<String> measurementTypes
    ) {}
}
