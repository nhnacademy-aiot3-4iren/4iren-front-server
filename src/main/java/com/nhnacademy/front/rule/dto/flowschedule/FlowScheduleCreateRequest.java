package com.nhnacademy.front.rule.dto.flowschedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public record FlowScheduleCreateRequest(
        @NotEmpty
        List<@Valid FlowScheduleRequest> flowScheduleRequestList
) {
        public record FlowScheduleRequest(

                @NotNull
                DayOfWeek dayOfWeek,

                @NotNull
                String startTime,

                @NotNull
                String endTime
        ){}
}
