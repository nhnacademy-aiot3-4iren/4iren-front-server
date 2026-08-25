package com.nhnacademy.front.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.notification.client.NotiAlertHistoryClient;
import com.nhnacademy.front.notification.dto.AlertHistoryFilterOptionsResponse;
import com.nhnacademy.front.notification.dto.AlertHistoryResponse;
import com.nhnacademy.front.notification.dto.AlertHistorySearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 알림 이력 조회 페이지 컨트롤러 (admin 전용).
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/alert-histories")
public class AlertHistoryController {

    private final NotiAlertHistoryClient notiAlertHistoryClient;

    /**
     * 알림 이력 목록 페이지. 검색 조건(roomId/botType/alertType/from~to)은 전부 optional.
     */
    @GetMapping
    public String getAlertHistories(
            AlertHistorySearchCondition condition,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "sendAt,DESC") String sort,
            Model model
    ) {
        Long roomId = condition.roomId();
        String botType = emptyToNull(condition.botType());
        String alertType = emptyToNull(condition.alertType());
        String from = condition.from() != null ? condition.from().toString() : null;
        String to = condition.to() != null ? condition.to().toString() : null;

        PageResponse<AlertHistoryResponse> alertHistories =
                notiAlertHistoryClient.getAllAlertHistory(roomId, botType, alertType, from, to, page, size, sort);
        AlertHistoryFilterOptionsResponse filterOptions = notiAlertHistoryClient.getFilterOptions();

        model.addAttribute("alertHistories", alertHistories);
        model.addAttribute("filterOptions", filterOptions);
        // 폼 선택 상태 유지 + 페이지네이션 링크용 (빈값은 null로 정규화)
        model.addAttribute("selRoomId", roomId);
        model.addAttribute("selBotType", botType);
        model.addAttribute("selAlertType", alertType);
        model.addAttribute("selFrom", from);
        model.addAttribute("selTo", to);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "alarms/alert-history";
    }

    /**
     * 알림 이력 단건 상세 페이지.
     */
    @GetMapping("/{alert-history-id}")
    public String getAlertHistory(
            @PathVariable("alert-history-id") Long alertHistoryId,
            Model model
    ) {
        AlertHistoryResponse alertHistory =
                notiAlertHistoryClient.getAlertHistoryById(alertHistoryId);

        model.addAttribute("alertHistory", alertHistory);

        return "alarms/alert-history-detail";
    }

    /**
     * 빈 문자열/공백을 null로 바꾼다. "전체" 선택(value="")이 백엔드 valueOf("")로 터지는 걸 막는다.
     */
    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
