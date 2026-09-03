package com.nhnacademy.front.core.dto.team.invitation;

import java.time.LocalDateTime;

public record TeamInvitationCodeSummaryResponse(
        Long invitationCodeId,
        Long teamId,
        LocalDateTime expiresAt,
        TeamInvitationCodeStatus status,
        LocalDateTime createdAt,
        Long createdBy
) {
}
