package com.nhnacademy.front.recommendation.client;

import com.nhnacademy.front.recommendation.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="4iren-gateway", path="/api/recommendation", contextId="recommendationClient")
public interface RecommendationClient {

    @PostMapping("/chat")
    LlmResponseDto getChatAnswer(@RequestBody LlmRequestDto request);

    @PostMapping("/welcome-briefing")
    WelcomeBriefingResponse getWelcomeBriefing(@RequestBody RoomBriefingRequest request);

    @PostMapping("/daily-summary")
    DailySummaryResponse getDailySummary(@RequestBody RoomBriefingRequest request);
}
