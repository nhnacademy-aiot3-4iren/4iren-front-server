package com.nhnacademy.front.service;

import com.nhnacademy.front.recommendation.client.RecommendationClient;
import com.nhnacademy.front.recommendation.dto.LlmRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final RecommendationClient recommendationClient;

    public String getApi(String message) {
        String tmp = recommendationClient.getChatAnswer(
                new LlmRequestDto(
                        "1",
                        null,
                        null,
                        message,
                        null,
                        null,
                        LocalDateTime.now()
                )
        ).answer();
        log.info("{}",tmp);

        return  tmp;
    }

}
