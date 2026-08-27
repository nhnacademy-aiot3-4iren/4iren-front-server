package com.nhnacademy.front.recommendation.service;

import com.nhnacademy.front.recommendation.client.RecommendationClient;
import com.nhnacademy.front.recommendation.dto.DailySummaryResponse;
import com.nhnacademy.front.recommendation.dto.RoomBriefingRequest;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomBriefingService {

    private final RecommendationClient recommendationClient;

    public Optional<WelcomeBriefingResponse> getWelcomeBriefing(Long teamId, Long roomId) {
        try {
            return Optional.ofNullable(recommendationClient.getWelcomeBriefing(
                    RoomBriefingRequest.welcome(teamId, roomId)
            ));
        } catch (FeignException e) {
            log.debug("Welcome briefing is not available. teamId={}, roomId={}, status={}",
                    teamId, roomId, e.status());
            return Optional.empty();
        }
    }

    public Optional<DailySummaryResponse> getDailySummary(Long teamId, Long roomId) {
        try {
            return Optional.ofNullable(recommendationClient.getDailySummary(
                    RoomBriefingRequest.dailySummary(teamId, roomId)
            ));
        } catch (FeignException e) {
            log.debug("Daily summary is not available. teamId={}, roomId={}, status={}",
                    teamId, roomId, e.status());
            return Optional.empty();
        }
    }
}
