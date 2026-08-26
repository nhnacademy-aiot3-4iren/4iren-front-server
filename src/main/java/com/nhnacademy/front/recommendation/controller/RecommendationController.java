package com.nhnacademy.front.recommendation.controller;

import com.nhnacademy.front.recommendation.dto.AnswerDto;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingRequest;
import com.nhnacademy.front.recommendation.dto.WelcomeBriefingResponse;
import com.nhnacademy.front.service.LlmService;
import com.nhnacademy.front.service.WelcomeBriefingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecommendationController {

    private final WelcomeBriefingService welcomeBriefingService;
    private final LlmService llmService;

    @GetMapping("/llm")
    public String getLlmPage() {
        return "llm";
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
    @PostMapping("/api/front/teams/{teamId}/rooms/{roomId}/welcome-briefing")
    @ResponseBody
    public ResponseEntity<WelcomeBriefingResponse> getWelcomeBriefing(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        try {
            WelcomeBriefingRequest request = new WelcomeBriefingRequest(teamId, roomId);
            return ResponseEntity.ok(welcomeBriefingService.getWelcomeBriefing(request));
        } catch (FeignException.NotFound exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
