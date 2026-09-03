package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.team.CoreTeamClient;
import com.nhnacademy.front.core.client.team.CoreTeamInvitationCodeClient;
import com.nhnacademy.front.core.client.team.CoreTeamMemberClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.*;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeCreateRequest;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeSummaryResponse;
import com.nhnacademy.front.core.dto.team.member.TeamJoinRequest;
import com.nhnacademy.front.core.dto.team.member.TeamMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int DEFAULT_INVITATION_EXPIRE_HOURS = 24;

    private final CoreTeamClient coreTeamClient;
    private final CoreTeamMemberClient coreTeamMemberClient;
    private final CoreTeamInvitationCodeClient coreTeamInvitationCodeClient;

    public PageResponse<TeamDetailResponse> getTeams(Integer page, Integer size, String sort) {
        PageResponse<TeamResponse> teams = coreTeamClient.getTeams(page, size, sort);
        List<TeamDetailResponse> details = teams.content().stream()
                .map(team -> coreTeamClient.getTeam(team.teamId()))
                .toList();

        return new PageResponse<>(
                details,
                teams.page(),
                teams.size(),
                teams.totalElements(),
                teams.totalPages(),
                teams.first(),
                teams.last()
        );
    }

    public List<TeamResponse> getAllTeams() {
        return coreTeamClient.getAllTeams();
    }

    public TeamResponse createTeam(TeamCreateRequest request) {
        return coreTeamClient.createTeam(request);
    }

    public TeamDetailResponse getTeam(Long teamId) {
        return coreTeamClient.getTeam(teamId);
    }

    public TeamResponse updateTeam(Long teamId, TeamUpdateRequest request) {
        return coreTeamClient.updateTeam(teamId, request);
    }

    public TeamResponse updateTeamStatus(Long teamId, TeamStatusUpdateRequest request) {
        return coreTeamClient.updateTeamStatus(teamId, request);
    }

    public void deleteTeam(Long teamId) {
        coreTeamClient.deleteTeam(teamId);
    }

    public TeamInvitationCodeResponse createInvitationCode(Long teamId, LocalDateTime expiresAt) {
        LocalDateTime resolvedExpiresAt = expiresAt != null
                ? expiresAt
                : LocalDateTime.now().plusHours(DEFAULT_INVITATION_EXPIRE_HOURS);

        return coreTeamInvitationCodeClient.createInvitationCode(
                teamId,
                new TeamInvitationCodeCreateRequest(resolvedExpiresAt)
        );
    }

    public List<TeamInvitationCodeSummaryResponse> getInvitationCodes(Long teamId) {
        return coreTeamInvitationCodeClient.getInvitationCodes(teamId);
    }

    public void deactivateInvitationCode(Long teamId, Long invitationCodeId) {
        coreTeamInvitationCodeClient.deactivateInvitationCode(teamId, invitationCodeId);
    }

    public TeamMemberResponse joinTeam(TeamJoinRequest request) {
        return coreTeamMemberClient.joinTeam(request);
    }

    public PageResponse<TeamMemberResponse> getTeamMembers(Long teamId, Integer page, Integer size, String sort) {
        return coreTeamMemberClient.getTeamMembers(teamId, page, size, sort);
    }

    public void removeTeamMember(Long teamId, Long teamMemberId) {
        coreTeamMemberClient.removeTeamMember(teamId, teamMemberId);
    }

    public void leaveTeam(Long teamId) {
        coreTeamMemberClient.leaveTeam(teamId);
    }
}
