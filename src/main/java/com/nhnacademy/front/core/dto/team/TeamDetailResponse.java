package com.nhnacademy.front.core.dto.team;

import java.time.LocalDateTime;

public record TeamDetailResponse(
        Long teamId,
        String teamName,
        String description,
        TeamStatus status,
        TeamStatusCause statusCause,
        LocalDateTime statusChangedAt,
        TeamRole myRole,
        long memberCount,
        long buildingCount,
        long roomCount,
        long sensorCount,
        long deviceCount
) {
}
