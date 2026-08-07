package com.nhnacademy.front.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.notification.client.NotiAlertHistoryClient;
import com.nhnacademy.front.notification.dto.AlertHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 알림 이력 조회 페이지 컨트롤러 (admin 전용).
 *
 * <p>딥링크와 달리 페이지(뷰)를 반환하므로 목록을 서버사이드에서 조회해 Model 에 담아 렌더링한다.
 * 관리자 권한 검증은 게이트웨이/역할 기반으로 처리되며, 프론트는 admin 메뉴에서만 진입하도록 가드한다.
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
}
