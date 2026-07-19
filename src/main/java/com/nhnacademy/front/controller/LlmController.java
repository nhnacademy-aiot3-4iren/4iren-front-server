package com.nhnacademy.front.controller;

import com.nhnacademy.front.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LlmController {

    private final LlmService llmService;

    @GetMapping("/llm")
    @ResponseBody
    public String getAnswer(@RequestParam(name = "message", defaultValue = "") String message) {
        log.info("{}",message);
        return llmService.getApi(message);
    }
}
