package com.nhnacademy.front.core.dto.team.member;

public record TeamMemberResponse(
        Long teamMemberId,
        Long teamId,
        Long userId
) {
}
