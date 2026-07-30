package com.nhnacademy.front.service;

import com.nhnacademy.front.recommendation.client.RecommendationClient;
import com.nhnacademy.front.recommendation.dto.AnswerDto;
import com.nhnacademy.front.recommendation.dto.LlmRequestDto;
import com.nhnacademy.front.recommendation.dto.LlmResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final RecommendationClient recommendationClient;

    public AnswerDto getApi(String message) {
        AnswerDto tmp = recommendationClient.getChatAnswer(
                new LlmRequestDto(
                        null,
                        null,
                        message,
                        LocalDateTime.now()
                )
        ).getAnswer();
        log.info("{}",tmp);

        return  tmp;
    }

}
