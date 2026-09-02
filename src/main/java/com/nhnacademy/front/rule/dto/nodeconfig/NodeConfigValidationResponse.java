package com.nhnacademy.front.rule.dto.nodeconfig;

import java.util.List;

public record NodeConfigValidationResponse(
        boolean valid,
        String message,
        List<NodeConfigError> errors
) {
    public record NodeConfigError(
            String field,
            String message
    ){}
}
