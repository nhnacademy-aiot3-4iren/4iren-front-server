package com.nhnacademy.front.core.dto.team.invitation;

import java.time.LocalDateTime;

public record TeamInvitationCodeResponse(
        Long invitationCodeId,
        Long teamId,
        String code,
        LocalDateTime expiresAt,
        boolean active
) {
}
