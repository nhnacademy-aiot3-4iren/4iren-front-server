package com.nhnacademy.front.core.dto.team.member;

import com.nhnacademy.front.core.dto.team.TeamRole;
import jakarta.validation.constraints.NotNull;

public record TeamMemberRoleChangeRequest(
        @NotNull(message = "팀 Role은 null일 수 없습니다.")
        TeamRole teamRole
) {
}
