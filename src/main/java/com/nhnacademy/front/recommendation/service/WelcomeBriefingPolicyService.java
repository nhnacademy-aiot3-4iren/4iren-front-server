package com.nhnacademy.front.recommendation.service;

import com.nhnacademy.front.recommendation.client.WelcomeBriefingPolicyClient;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyEnabledRequest;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyDto;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingPolicyResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WelcomeBriefingPolicyService {

    private final WelcomeBriefingPolicyClient policyClient;

    public WelcomeBriefingPolicyDto getPolicy(Long teamId, Long roomId) {
        validateScope(teamId, roomId);
        return policyClient.getPolicy(teamId, roomId);
    }

    public WelcomeBriefingPolicyResponse createPolicy(
            Long teamId, Long roomId, WelcomeBriefingPolicyDto request
    ) {
        validateScope(teamId, roomId);
        return policyClient.createPolicy(teamId, roomId, request);
    }

    public WelcomeBriefingPolicyResponse savePolicy(
            Long teamId, Long roomId, WelcomeBriefingPolicyDto request
    ) {
        validateScope(teamId, roomId);
        try {
            return policyClient.updatePolicy(teamId, roomId, request);
        } catch (FeignException.NotFound exception) {
            return policyClient.createPolicy(teamId, roomId, request);
        }
    }

    public void updatePolicyEnabled(
            Long teamId, Long roomId, WelcomeBriefingPolicyEnabledRequest request
    ) {
        validateScope(teamId, roomId);
        policyClient.updatePolicyEnabled(teamId, roomId, request);
    }

    public void deletePolicy(Long teamId, Long roomId) {
        validateScope(teamId, roomId);
        policyClient.deletePolicy(teamId, roomId);
    }

    private void validateScope(Long teamId, Long roomId) {
        if (teamId != null && teamId <= 0) {
            throw new IllegalArgumentException("teamId must be positive");
        }
        if (roomId != null && roomId <= 0) {
            throw new IllegalArgumentException("roomId must be positive");
        }
        if (roomId != null && teamId == null) {
            throw new IllegalArgumentException("teamId is required when roomId is provided");
        }
    }
}
