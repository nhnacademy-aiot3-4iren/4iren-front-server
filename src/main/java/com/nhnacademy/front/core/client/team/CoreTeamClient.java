package com.nhnacademy.front.core.client.team;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.TeamCreateRequest;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.dto.team.TeamResponse;
import com.nhnacademy.front.core.dto.team.TeamUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreTeamClient",
        path = "/api/core/teams"
)
public interface CoreTeamClient {

    @PostMapping
    TeamResponse createTeam(@RequestBody TeamCreateRequest request);

    @GetMapping
    PageResponse<TeamResponse> getTeams(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @GetMapping("/all")
    List<TeamResponse> getAllTeams();

    @GetMapping("/{teamId}")
    TeamDetailResponse getTeam(@PathVariable("teamId") Long teamId);

    @PatchMapping("/{teamId}")
    TeamResponse updateTeam(
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamUpdateRequest request
    );

    @DeleteMapping("/{teamId}")
    void deleteTeam(@PathVariable("teamId") Long teamId);
}
