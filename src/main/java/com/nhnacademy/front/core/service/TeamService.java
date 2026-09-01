package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.team.CoreTeamClient;
import com.nhnacademy.front.core.client.team.CoreTeamInvitationCodeClient;
import com.nhnacademy.front.core.client.team.CoreTeamMemberClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.TeamCreateRequest;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.dto.team.TeamUpdateRequest;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeCreateRequest;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import com.nhnacademy.front.core.dto.team.member.TeamJoinRequest;
import com.nhnacademy.front.core.dto.team.member.TeamMemberResponse;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
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

    public TeamResponse updateTeam(Long teamId, TeamCreateRequest request) {
        TeamUpdateRequest updateRequest = new TeamUpdateRequest();
        updateRequest.setTeamName(JsonNullable.of(request.teamName()));
        updateRequest.setDescription(JsonNullable.of(request.description()));

        return coreTeamClient.updateTeam(teamId, updateRequest);
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

    public TeamMemberResponse joinTeam(TeamJoinRequest request) {
        return coreTeamMemberClient.joinTeam(request);
    }

    public PageResponse<TeamMemberResponse> getTeamMembers(Long teamId, Integer page, Integer size, String sort) {
        return coreTeamMemberClient.getTeamMembers(teamId, page, size, sort);
    }
}
