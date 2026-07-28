package com.nhnacademy.front.recommendation.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record LlmAnswerDto(
        Long userId,
        Long roomId,
        String message,
        String answer,
        LocalDateTime requestedAt,
        LocalDateTime receivedAt,
        LocalDateTime answeredAt
){
//    public LlmAnswerDto(String message, String answer, LocalDateTime requestedAt) {
//        this(message, answer, requestedAt, LocalDateTime.now());
//    }
}