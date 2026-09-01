package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.TeamCreateRequest;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.dto.team.invitation.TeamInvitationCodeResponse;
import com.nhnacademy.front.core.dto.team.member.TeamJoinRequest;
import com.nhnacademy.front.core.dto.team.member.TeamMemberResponse;
import com.nhnacademy.front.core.service.TeamService;
import com.nhnacademy.front.payment.client.PaymentClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
            @Valid @RequestBody TeamCreateRequest request
    ) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, request));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createInvitationCode(teamId, expiresAt));
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

    public record InvitationCodeRequest(LocalDateTime expiresAt) {
    }
}
