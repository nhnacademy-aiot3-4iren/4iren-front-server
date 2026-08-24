package com.nhnacademy.front.core.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Cookie[] cookies = request.getCookies();
            if (cookies != null && !template.headers().containsKey("Authorization")) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                        template.header("Authorization", "Bearer " + cookie.getValue());
                        break;
                    }
                }
            }
        }
    }
}
