package com.nhnacademy.front.core.client.team;

import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeCreateRequest;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreTeamInvitationCodeClient",
        path = "/api/core/teams"
)
public interface CoreTeamInvitationCodeClient {

    @GetMapping("/{teamId}/invitation-codes")
    List<TeamInvitationCodeSummaryResponse> getInvitationCodes(
            @PathVariable("teamId") Long teamId
    );

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
