package com.nhnacademy.front.rule.dto;

/**
 * 화면 JS가 실패 시 읽어가는 응답. body.message 만 사용한다.
 */
public record ApiErrorResponse(
        String message
) {
}