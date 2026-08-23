package com.nhnacademy.front.account.dto.user;

import java.time.LocalDateTime;

// 마이페이지/회원정보 조회 dto
public record UserResponseDto (
     Long userId,
     String loginId,
     String email,
     String name,
     String role,     // 예: ROLE_USER, ROLE_ADMIN
     String status,  // 예: ACTIVE, DORMANT, WITHDRAWN
     LocalDateTime createdAt
){}