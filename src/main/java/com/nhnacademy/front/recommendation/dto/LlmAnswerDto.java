package com.nhnacademy.front.recommendation.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record LlmAnswerDto(
       String message,
       String answer,
       LocalDateTime requestedAt,
       LocalDateTime answeredAt
){
    public LlmAnswerDto(String message, String answer, LocalDateTime requestedAt) {
        this(message, answer, requestedAt, LocalDateTime.now());
    }
}