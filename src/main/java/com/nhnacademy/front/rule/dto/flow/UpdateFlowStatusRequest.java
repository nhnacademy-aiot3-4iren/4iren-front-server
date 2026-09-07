package com.nhnacademy.front.rule.dto.flow;

import jakarta.validation.constraints.NotNull;

public record UpdateFlowStatusRequest(
        @NotNull Boolean isActive
) {}