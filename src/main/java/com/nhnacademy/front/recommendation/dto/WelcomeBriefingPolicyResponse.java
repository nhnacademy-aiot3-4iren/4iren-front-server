package com.nhnacademy.front.recommendation.dto;

import java.time.LocalDateTime;

public record WelcomeBriefingPolicyResponse(
        Long id,
        Long teamId,
        Long roomId,
        Integer rainPossibleProbability,
        Integer rainExpectedProbability,
        Double strongWindSpeed,
        Integer highHumidityPercent,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
