package com.nhnacademy.front.recommendation.controller;

import com.nhnacademy.front.recommendation.client.RecommendationClient;
import com.nhnacademy.front.recommendation.dto.LlmAnswerDto;
import com.nhnacademy.front.recommendation.dto.LlmRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationClient recommendationClient;

    @GetMapping("/llm")
    public String getLlmPage() {
        return "llm";
    }

    @PostMapping("/llm/answer")
    @ResponseBody
    public String postLlmAnswer(
            @RequestParam String message
    ) {
        LlmRequestDto request = new LlmRequestDto("123", message, LocalDateTime.now());
        LlmAnswerDto answerDto=recommendationClient.getChatAnswer(request);
        String answer=answerDto.answer();

        log.info("Received answer: {}", answer);

        return answer;
    }
}
