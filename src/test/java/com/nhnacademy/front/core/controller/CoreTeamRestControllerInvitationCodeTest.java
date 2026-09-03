package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeStatus;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeSummaryResponse;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.payment.client.PaymentClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CoreTeamRestControllerInvitationCodeTest {

    private final TeamService teamService = mock(TeamService.class);
    private final PaymentClient paymentClient = mock(PaymentClient.class);
    private final CoreTeamRestController controller = new CoreTeamRestController(teamService, paymentClient);

    @Test
    void getsInvitationCodeList() {
        List<TeamInvitationCodeSummaryResponse> invitationCodes = List.of(summary());
        when(teamService.getInvitationCodes(1L)).thenReturn(invitationCodes);

        ResponseEntity<List<TeamInvitationCodeSummaryResponse>> response = controller.getInvitationCodes(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(invitationCodes);
        verify(teamService).getInvitationCodes(1L);
    }

    @Test
    void deactivatesInvitationCode() {
        ResponseEntity<Void> response = controller.deactivateInvitationCode(1L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(teamService).deactivateInvitationCode(1L, 10L);
    }

    @Test
    void keepsInvitationCodeCreation() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 4, 10, 0);
        TeamInvitationCodeResponse created = new TeamInvitationCodeResponse(
                10L, 1L, "ABCDEFGH", expiresAt, true
        );
        when(teamService.createInvitationCode(1L, expiresAt)).thenReturn(created);

        ResponseEntity<TeamInvitationCodeResponse> response = controller.createInvitationCode(
                1L, new CoreTeamRestController.InvitationCodeRequest(expiresAt)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
        verify(teamService).createInvitationCode(1L, expiresAt);
    }

    private TeamInvitationCodeSummaryResponse summary() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 3, 10, 0);
        return new TeamInvitationCodeSummaryResponse(
                10L, 1L, now.plusDays(1), TeamInvitationCodeStatus.AVAILABLE, now, 7L
        );
    }
}
