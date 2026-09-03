package com.nhnacademy.front.owner.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String loginId,
        String role,
        String email,
        String name,
        String status,
        LocalDateTime createdAt
) {}
