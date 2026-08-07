package com.nhnacademy.front.notification.dto;

public record LinkTokenResponse(
        String deepLinkUrl,
        long expiresInSeconds) {
}