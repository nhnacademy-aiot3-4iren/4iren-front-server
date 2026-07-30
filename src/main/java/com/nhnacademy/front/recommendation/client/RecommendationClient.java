package com.nhnacademy.front.recommendation.client;

import com.nhnacademy.front.recommendation.dto.LlmAnswerDto;
import com.nhnacademy.front.recommendation.dto.LlmRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "4iren-gateway",
        url = "${api.recommendation-url}",
        path = "/api/recommendation",
        contextId = "recommendationClient"
)
public interface RecommendationClient {

    @PostMapping("/chat")
    LlmAnswerDto getChatAnswer(@RequestBody LlmRequestDto request);
}
