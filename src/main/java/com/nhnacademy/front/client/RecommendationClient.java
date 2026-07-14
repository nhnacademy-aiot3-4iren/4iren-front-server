package com.nhnacademy.front.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "recommendation-service", url ="${api.recommendation-url}")
public interface RecommendationClient {

    @GetMapping("/llm")
    String getAnswer(@RequestParam String message);

}
