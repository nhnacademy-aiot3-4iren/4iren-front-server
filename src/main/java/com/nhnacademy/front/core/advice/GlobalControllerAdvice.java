package com.nhnacademy.front.core.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.team.TeamDetailResponse;
import com.nhnacademy.front.core.service.TeamService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ObjectMapper objectMapper;
    private final TeamService teamService;

    @ModelAttribute("loginId")
    public String getLoginId(HttpServletRequest request) {
        return extractClaim(request, "login-id");
    }

    @ModelAttribute("userId")
    public Long getUserId(HttpServletRequest request) {
        String sub = extractClaim(request, "sub");
        return (sub != null && !sub.isEmpty()) ? Long.valueOf(sub) : null;
    }

    @ModelAttribute("role")
    public String getRole(HttpServletRequest request) {
        String role = extractClaim(request, "role");
        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        return role;
    }

    /**
     * sidebar의 Team Info / Members / Buildings / Classrooms 링크가 사용할 기본 teamId.
     * 유저가 여러 팀에 속할 수 있어서 "어느 팀을 기본으로 보여줄지"가 애매한데,
     * 지금은 내 팀 목록(getTeams) 중 정렬 기준 첫 번째 팀을 임시로 사용함.
     * 개선할 수 있음. 지금은 로그인 안 했거나 소속 팀이 없는 경우(회원가입 직후 등)
     * null을 반환하고, sidebar 쪽 링크는 TeamController가 teamId 없이 들어오면
     * /team으로 리다이렉트하도록 이미 처리되어 있어서 에러 없이 넘어감.
     */
    @ModelAttribute("currentTeamId")
    public Long getCurrentTeamId(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return null;
        }

        try {
            PageResponse<TeamDetailResponse> teams = teamService.getTeams(0, 1, "id,ASC");
            if (teams != null && teams.content() != null && !teams.content().isEmpty()) {
                return teams.content().get(0).teamId();
            }
        } catch (Exception e) {
            // 팀이 없는 유저(회원가입 직후 등)일 수 있으므로 조용히 넘어감
            log.debug("Failed to resolve currentTeamId: {}", e.getMessage());
        }
        return null;
    }

    private String extractClaim(HttpServletRequest request, String claimKey) {
        String token = (String) request.getAttribute("newAccessToken");

        if (token == null || token.isEmpty()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token != null && !token.isEmpty()) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            try {
                String[] parts = token.split("\\.");
                if (parts.length > 1) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    JsonNode jsonNode = objectMapper.readTree(payload);
                    if (jsonNode.has(claimKey)) {
                        return jsonNode.get(claimKey).asText();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to decode JWT payload: {}", e.getMessage());
            }
        }
        return null;
    }
}