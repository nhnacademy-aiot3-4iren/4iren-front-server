package com.nhnacademy.front.core.dto.team.member;

import com.nhnacademy.front.core.dto.team.TeamRole;

public record TeamMemberResponse(
        Long teamMemberId,
        Long teamId,
        Long userId,
        TeamRole teamRole
) {
}
