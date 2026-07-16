package com.nhnacademy.front.recommendation.client;

import com.nhnacademy.front.recommendation.dto.LlmAnswerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="4iren-gateway", path="/api/recommendation", contextId="recommendationClient")
public interface RecommendationClient {

    @GetMapping("/llm")
    LlmAnswerDto getLlmAnswer(@RequestParam(name="message") String message);
}
