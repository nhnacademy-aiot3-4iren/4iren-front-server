package com.nhnacademy.front.core.client.team;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.member.TeamJoinRequest;
import com.nhnacademy.front.core.dto.team.member.TeamMemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreTeamMemberClient",
        path = "/api/core"
)
public interface CoreTeamMemberClient {

    @PostMapping("/team-memberships")
    TeamMemberResponse joinTeam(@RequestBody TeamJoinRequest request);

    @GetMapping("/teams/{teamId}/members")
    PageResponse<TeamMemberResponse> getTeamMembers(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @DeleteMapping("/teams/{teamId}/members/{teamMemberId}")
    void removeTeamMember(
            @PathVariable("teamId") Long teamId,
            @PathVariable("teamMemberId") Long teamMemberId
    );

    @DeleteMapping("/teams/{teamId}/members/me")
    void leaveTeam(@PathVariable("teamId") Long teamId);
}
