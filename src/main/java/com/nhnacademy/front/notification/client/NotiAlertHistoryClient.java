package com.nhnacademy.front.notification.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.notification.dto.AlertHistoryFilterOptionsResponse;
import com.nhnacademy.front.notification.dto.AlertHistoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "4iren-gateway",
        contextId = "notiAlertHistoryClient",
        path = "/api/notification/alert-histories"
)
public interface NotiAlertHistoryClient {

    @GetMapping
    PageResponse<AlertHistoryResponse> getAllAlertHistory(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "botType", required = false) String botType,
            @RequestParam(name = "alertType", required = false) String alertType,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "sendAt,DESC") String sort
    );

    @GetMapping("/filter-options")
    AlertHistoryFilterOptionsResponse getFilterOptions();

    @GetMapping("/{alert-history-id}")
    AlertHistoryResponse getAlertHistoryById(
            @PathVariable("alert-history-id") Long alertHistoryId
    );

}
