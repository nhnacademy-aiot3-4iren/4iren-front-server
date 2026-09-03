package com.nhnacademy.front.recommendation.controller;

import com.nhnacademy.front.recommendation.dto.AnswerDto;
import com.nhnacademy.front.recommendation.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecommendationController {

    private final LlmService llmService;

    @GetMapping("/llm")
    public String getLlmPage() {
        return "llm/llm";
    }

    @PostMapping("/llm/answer")
    @ResponseBody
    public AnswerDto postLlmAnswer(
            @RequestParam String message
    ) {
        AnswerDto answer=llmService.getApi(message);

        log.info("Received answer: {}", answer);

        return answer;
    }
}
