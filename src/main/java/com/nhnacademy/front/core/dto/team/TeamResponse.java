package com.nhnacademy.front.core.dto.team;

public record TeamResponse(
        Long teamId,
        String teamName,
        String description,
        TeamRole myRole
) {
}
