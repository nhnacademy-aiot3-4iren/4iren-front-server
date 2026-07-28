package com.nhnacademy.front.recommendation.dto;

import java.time.LocalDateTime;

public record LlmRequestDto(
        String userId,
        String message,
        LocalDateTime requestedAt
) {
}
