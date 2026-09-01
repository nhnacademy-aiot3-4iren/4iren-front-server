package com.nhnacademy.front.recommendation.controller;

import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyDto;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyEnabledRequest;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyResponse;
import com.nhnacademy.front.recommendation.service.WelcomeBriefingPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/welcome-briefing/policies")
public class WelcomeBriefingPolicyController {

    private final WelcomeBriefingPolicyService policyService;

    @GetMapping
    public ResponseEntity<WelcomeBriefingPolicyDto> getPolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId
    ) {
        return ResponseEntity.ok(policyService.getPolicy(teamId, roomId));
    }

    @PostMapping
    public ResponseEntity<WelcomeBriefingPolicyResponse> createPolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @Valid @RequestBody WelcomeBriefingPolicyDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.createPolicy(teamId, roomId, request));
    }

    @PutMapping
    public ResponseEntity<WelcomeBriefingPolicyResponse> savePolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @Valid @RequestBody WelcomeBriefingPolicyDto request
    ) {
        return ResponseEntity.ok(policyService.savePolicy(teamId, roomId, request));
    }

    @PatchMapping("/enabled")
    public ResponseEntity<Void> updatePolicyEnabled(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @Valid @RequestBody WelcomeBriefingPolicyEnabledRequest request
    ) {
        policyService.updatePolicyEnabled(teamId, roomId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId
    ) {
        policyService.deletePolicy(teamId, roomId);
        return ResponseEntity.noContent().build();
    }
}
