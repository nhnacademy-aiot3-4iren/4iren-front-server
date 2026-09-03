package com.nhnacademy.front.core.config;

import com.nhnacademy.front.auth.service.AuthService;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Slf4j
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    private final AuthService authService;
    private final ErrorDecoder defaultErrorDecoder = new Default();

    public FeignErrorDecoder(@Lazy AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        // AuthClient의 refresh 요청 자체가 401인 경우는 무한루프 방지를 위해 바로 통과
        if (methodKey.contains("AuthClient#refreshToken")) {
            return defaultErrorDecoder.decode(methodKey, response);
        }

        if (response.status() == 401) {
            log.warn("Feign request got 401 Unauthorized for method: {}. Attempting to refresh token...", methodKey);
            
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                HttpServletResponse httpResponse = attributes.getResponse();
                
                String refreshToken = null;
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("refreshToken".equals(cookie.getName())) {
                            refreshToken = cookie.getValue();
                            break;
                        }
                    }
                }

                if (refreshToken != null && !refreshToken.isEmpty()) {
                    boolean refreshed = authService.refresh(refreshToken, httpResponse);
                    if (refreshed) {
                        log.info("Token refreshed successfully inside ErrorDecoder. Retrying the request...");
                        // 토큰 갱신에 성공하면 RetryableException을 던져서 Feign이 즉시 다시 요청하게 만듦
                        return new RetryableException(
                                response.status(),
                                "Access token expired or blacklisted, token refreshed. Retrying...",
                                response.request().httpMethod(),
                                new Date(), // 즉시 재시도
                                response.request()
                        );
                    } else {
                        log.error("Failed to refresh token inside ErrorDecoder.");
                    }
                }
            }
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}
