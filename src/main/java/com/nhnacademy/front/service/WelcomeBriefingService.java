package com.nhnacademy.front.service;

import com.nhnacademy.front.recommendation.client.RecommendationClient;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingRequest;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WelcomeBriefingService {
    private final RecommendationClient recommendationClient;

    public WelcomeBriefingResponse getWelcomeBriefing(WelcomeBriefingRequest welcomeBriefingRequest) {
        return recommendationClient.getWelcomeBriefing(welcomeBriefingRequest);
    }
}
