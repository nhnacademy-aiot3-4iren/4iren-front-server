package com.nhnacademy.front.client;

import com.nhnacademy.front.dto.LlmDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//
@FeignClient(name = "recommendation-service", url ="${api.recommendation-url}")
public interface RecommendationClient {

    @GetMapping("/llm")
    LlmDto getAnswer(@RequestParam String message);

}
