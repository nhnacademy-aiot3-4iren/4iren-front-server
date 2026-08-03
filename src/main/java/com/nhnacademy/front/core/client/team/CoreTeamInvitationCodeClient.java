package com.nhnacademy.front.core.client.team;

import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeCreateRequest;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreTeamInvitationCodeClient",
        path = "/api/core/teams"
)
public interface CoreTeamInvitationCodeClient {

    @PostMapping("/{teamId}/invitation-codes")
    TeamInvitationCodeResponse createInvitationCode(
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamInvitationCodeCreateRequest request
    );

    @DeleteMapping("/{teamId}/invitation-codes/{invitationCodeId}")
    void deactivateInvitationCode(
            @PathVariable("teamId") Long teamId,
            @PathVariable("invitationCodeId") Long invitationCodeId
    );
}
