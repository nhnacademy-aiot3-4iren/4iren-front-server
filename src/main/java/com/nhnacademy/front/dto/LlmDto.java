package com.nhnacademy.front.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LlmDto {
    private final String message;
    private final String answer;
    private final LocalDateTime requestedAt;
    private final LocalDateTime answeredAt;

    public LlmDto(String message, String answer, LocalDateTime requestedAt) {
        this.message = message;
        this.answer = answer;
        this.requestedAt = requestedAt;
        answeredAt = LocalDateTime.now();
    }
}
