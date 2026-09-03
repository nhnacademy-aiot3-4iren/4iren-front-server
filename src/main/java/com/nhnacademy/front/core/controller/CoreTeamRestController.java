package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.*;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeSummaryResponse;
import com.nhnacademy.front.core.dto.team.member.TeamJoinRequest;
import com.nhnacademy.front.core.dto.team.member.TeamMemberResponse;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.payment.client.PaymentClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams")
public class CoreTeamRestController {

    private static final String NORMAL = "NORMAL";

    private final TeamService teamService;
    private final PaymentClient paymentClient;

    @GetMapping
    public ResponseEntity<PageResponse<TeamDetailResponse>> getTeams(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(teamService.getTeams(page, size, sort));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody TeamCreateRequest request,
            @ModelAttribute("role") String role
    ) {
        if (NORMAL.equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(request));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamUpdateRequest request
    ) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, request));
    }

    @PatchMapping("/{teamId}/status")
    public ResponseEntity<TeamResponse> updateTeamStatus(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(teamService.updateTeamStatus(teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/invitation-codes")
    public ResponseEntity<TeamInvitationCodeResponse> createInvitationCode(
            @PathVariable Long teamId,
            @RequestBody(required = false) InvitationCodeRequest request
    ) {
        LocalDateTime expiresAt = request != null ? request.expiresAt() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(teamService.createInvitationCode(teamId, expiresAt));
    }

    @GetMapping("/{teamId}/invitation-codes")
    public ResponseEntity<List<TeamInvitationCodeSummaryResponse>> getInvitationCodes(
            @PathVariable Long teamId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(teamService.getInvitationCodes(teamId));
    }

    @DeleteMapping("/{teamId}/invitation-codes/{invitationCodeId}")
    public ResponseEntity<Void> deactivateInvitationCode(
            @PathVariable Long teamId,
            @PathVariable Long invitationCodeId
    ) {
        teamService.deactivateInvitationCode(teamId, invitationCodeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/memberships")
    public ResponseEntity<TeamMemberResponse> joinTeam(@Valid @RequestBody TeamJoinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.joinTeam(request));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<PageResponse<TeamMemberResponse>> getTeamMembers(
            @PathVariable Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(teamService.getTeamMembers(teamId, page, size, sort));
    }

    @DeleteMapping("/{teamId}/members/{teamMemberId}")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long teamMemberId
    ) {
        teamService.removeTeamMember(teamId, teamMemberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teamId}/members/me")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long teamId) {
        teamService.leaveTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    public record InvitationCodeRequest(LocalDateTime expiresAt) {
    }
}
