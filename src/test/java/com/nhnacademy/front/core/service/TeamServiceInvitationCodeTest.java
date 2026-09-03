package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.team.CoreTeamClient;
import com.nhnacademy.front.core.client.team.CoreTeamInvitationCodeClient;
import com.nhnacademy.front.core.client.team.CoreTeamMemberClient;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeStatus;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TeamServiceInvitationCodeTest {

    private final CoreTeamClient teamClient = mock(CoreTeamClient.class);
    private final CoreTeamMemberClient memberClient = mock(CoreTeamMemberClient.class);
    private final CoreTeamInvitationCodeClient invitationCodeClient = mock(CoreTeamInvitationCodeClient.class);
    private final TeamService service = new TeamService(teamClient, memberClient, invitationCodeClient);

    @Test
    void delegatesInvitationCodeListToClient() {
        List<TeamInvitationCodeSummaryResponse> invitationCodes = List.of(summary());
        when(invitationCodeClient.getInvitationCodes(1L)).thenReturn(invitationCodes);

        assertThat(service.getInvitationCodes(1L)).isSameAs(invitationCodes);
        verify(invitationCodeClient).getInvitationCodes(1L);
    }

    @Test
    void delegatesInvitationCodeDeactivationToClient() {
        service.deactivateInvitationCode(1L, 10L);

        verify(invitationCodeClient).deactivateInvitationCode(1L, 10L);
    }

    private TeamInvitationCodeSummaryResponse summary() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 3, 10, 0);
        return new TeamInvitationCodeSummaryResponse(
                10L, 1L, now.plusDays(1), TeamInvitationCodeStatus.AVAILABLE, now, 7L
        );
    }
}
