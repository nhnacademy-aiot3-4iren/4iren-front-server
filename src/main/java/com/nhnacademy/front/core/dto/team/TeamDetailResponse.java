package com.nhnacademy.front.core.dto.team;

public record TeamDetailResponse(
        Long teamId,
        String teamName,
        String description,
        TeamRole myRole,
        long memberCount,
        long buildingCount,
        long roomCount,
        long sensorCount,
        long deviceCount
) {
}
