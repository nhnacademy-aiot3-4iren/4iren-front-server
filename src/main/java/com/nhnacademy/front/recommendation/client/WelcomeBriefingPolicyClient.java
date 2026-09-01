package com.nhnacademy.front.recommendation.client;

import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyEnabledRequest;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyDto;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "welcomeBriefingPolicyClient",
        path = "/api/recommendation/welcome-briefing/policies"
)
public interface WelcomeBriefingPolicyClient {

    @GetMapping
    WelcomeBriefingPolicyDto getPolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId
    );

    @PostMapping
    WelcomeBriefingPolicyResponse createPolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestBody WelcomeBriefingPolicyDto request
    );

    @PutMapping
    WelcomeBriefingPolicyResponse updatePolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestBody WelcomeBriefingPolicyDto request
    );

    @PatchMapping("/enabled")
    void updatePolicyEnabled(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestBody WelcomeBriefingPolicyEnabledRequest request
    );

    @DeleteMapping
    void deletePolicy(
            @RequestParam(name = "teamId", required = false) Long teamId,
            @RequestParam(name = "roomId", required = false) Long roomId
    );
}
