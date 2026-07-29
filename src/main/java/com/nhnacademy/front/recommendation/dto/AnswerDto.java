package com.nhnacademy.front.recommendation.dto;

import java.util.List;

public record AnswerDto(
        String answer,
        List<String> options
) {
}
