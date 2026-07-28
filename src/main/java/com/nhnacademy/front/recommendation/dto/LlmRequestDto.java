package com.nhnacademy.front.recommendation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LlmRequestDto(
        Long lastMentionRoomId,
        List<Long> subscribedRoomIds,
        String message,
        LocalDateTime requestedAt
) {
}
