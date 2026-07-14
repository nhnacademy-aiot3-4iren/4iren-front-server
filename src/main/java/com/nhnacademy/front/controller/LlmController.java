package com.nhnacademy.front.controller;

import com.nhnacademy.front.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;

    @GetMapping("/llm")
    @ResponseBody
    public String getAnswer(@ModelAttribute String message) {
        return llmService.getApi(message);
    }
}
