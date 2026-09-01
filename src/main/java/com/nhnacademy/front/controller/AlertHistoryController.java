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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * 알림 이력 조회 페이지 컨트롤러. 자기한테 온 알림 이력만 보이므로 role 무관하게 접근 가능.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/alert-histories")
public class AlertHistoryController {

    // 페이지네이션 바에서 현재 페이지 기준 앞뒤로 보여줄 페이지 수 (첫/끝 페이지는 항상 별도로 보장)
    private static final int PAGE_WINDOW = 2;

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
        model.addAttribute("pageWindow", buildPageWindow(alertHistories.page(), alertHistories.totalPages()));

        return "alarms/alert-history";
    }

    /**
     * 페이지네이션 바에 뿌릴 페이지 번호 목록을 계산한다. 전체 페이지를 다 나열하지 않고
     * 첫/끝 페이지 + 현재 페이지 주변({@value #PAGE_WINDOW}개씩)만 남기고 나머지는 null(생략 표시)로 묶는다.
     * 추가 조회 없이 이미 응답에 있는 page/totalPages 숫자만으로 계산한다.
     */
    private List<Integer> buildPageWindow(int currentPage, int totalPages) {
        if (totalPages <= 0) {
            return List.of();
        }

        TreeSet<Integer> shown = new TreeSet<>();
        shown.add(0);
        shown.add(totalPages - 1);
        for (int i = Math.max(0, currentPage - PAGE_WINDOW); i <= Math.min(totalPages - 1, currentPage + PAGE_WINDOW); i++) {
            shown.add(i);
        }

        List<Integer> window = new ArrayList<>();
        Integer previous = null;
        for (Integer i : shown) {
            if (previous != null && i - previous > 1) {
                window.add(null); // 생략 표시
            }
            window.add(i);
            previous = i;
        }
        return window;
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
