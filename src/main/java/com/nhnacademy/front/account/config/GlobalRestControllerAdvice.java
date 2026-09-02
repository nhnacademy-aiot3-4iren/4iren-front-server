package com.nhnacademy.front.account.config;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = {
        "com.nhnacademy.front.account.controller",
        "com.nhnacademy.front.auth.controller"
})
public class GlobalRestControllerAdvice {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> handleFeignException(FeignException e) {
        log.warn("API Error (Feign): {}", e.getMessage());
        String errorMessage = FeignErrorParser.getMessage(e, "요청 처리에 실패했습니다.");
        return ResponseEntity.status(e.status()).body(Map.of("message", errorMessage));
    }
}
