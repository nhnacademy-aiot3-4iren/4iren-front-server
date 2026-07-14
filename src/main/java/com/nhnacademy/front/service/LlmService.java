package com.nhnacademy.front.service;

import com.nhnacademy.front.client.RecommendationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmService {

    private final RecommendationClient recommendationClient;

    public String getApi(String message) {
        return  recommendationClient.getAnswer(message);
    }

}
