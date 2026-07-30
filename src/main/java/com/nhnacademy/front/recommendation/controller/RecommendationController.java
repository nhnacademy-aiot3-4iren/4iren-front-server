package com.nhnacademy.front.recommendation.controller;

import com.nhnacademy.front.recommendation.client.RecommendationClient;
import com.nhnacademy.front.recommendation.dto.LlmAnswerDto;
import com.nhnacademy.front.recommendation.dto.LlmRequestDto;
import com.nhnacademy.front.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecommendationController {

    private final LlmService llmService;

    @GetMapping("/llm")
    public String getLlmPage() {
        return "llm";
    }

    @PostMapping("/llm/answer")
    @ResponseBody
    public String postLlmAnswer(
            @RequestParam String message
    ) {
        String answer=llmService.getApi(message);

        log.info("Received answer: {}", answer);

        return answer;
    }
}
