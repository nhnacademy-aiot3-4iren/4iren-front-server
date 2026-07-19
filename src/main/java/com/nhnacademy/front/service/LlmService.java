package com.nhnacademy.front.service;

import com.nhnacademy.front.client.RecommendationClient;
import com.nhnacademy.front.dto.LlmDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final RecommendationClient recommendationClient;

    public String getApi(String message) {
        String tmp = recommendationClient.getAnswer(message).getAnswer();
        log.info("{}",tmp);
        return  recommendationClient.getAnswer(message).getAnswer();

    }

}
