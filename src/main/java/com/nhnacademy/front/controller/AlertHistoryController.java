package com.nhnacademy.front.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.notification.client.NotiAlertHistoryClient;
import com.nhnacademy.front.notification.dto.AlertHistoryResponse;
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

    @GetMapping
    public String getAlertHistories(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "sendAt,DESC") String sort,
            Model model
    ) {
        PageResponse<AlertHistoryResponse> alertHistories =
                notiAlertHistoryClient.getAllAlertHistory(page, size, sort);

        model.addAttribute("alertHistories", alertHistories);

        return "alert-history";
    }

    @GetMapping("/{alert-history-id}")
    public String getAlertHistory(
            @PathVariable("alert-history-id") Long alertHistoryId,
            Model model
    ) {
        AlertHistoryResponse alertHistory =
                notiAlertHistoryClient.getAlertHistoryById(alertHistoryId);

        model.addAttribute("alertHistory", alertHistory);

        return "alert-history-detail";
    }
}
