package com.nhnacademy.front.rule.dto.flow;

import jakarta.validation.constraints.NotNull;

public class UpdateFlowStatusRequest {
    @NotNull
    Boolean isActive;
}
