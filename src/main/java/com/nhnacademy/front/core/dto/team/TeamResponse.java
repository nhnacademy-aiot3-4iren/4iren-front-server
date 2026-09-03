package com.nhnacademy.front.core.dto.team;

import java.time.LocalDateTime;

public record TeamResponse(
        Long teamId,
        String teamName,
        String description,
        TeamStatus status,
        TeamStatusCause statusCause,
        LocalDateTime statusChangedAt,
        TeamRole myRole
) {
}
