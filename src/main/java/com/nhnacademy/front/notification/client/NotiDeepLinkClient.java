package com.nhnacademy.front.notification.client;

import com.nhnacademy.front.notification.dto.LinkStatusResponse;
import com.nhnacademy.front.notification.dto.LinkTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "4iren-gateway",
        contextId = "notiDeepLinkClient",
        path = "/api/notification/telegram"
)
public interface NotiDeepLinkClient {

    @PostMapping("/admin/link-token")
    LinkTokenResponse getlinkAdminToken();

    @PostMapping("/member/link-token")
    LinkTokenResponse getlinkMemberToken();

    @GetMapping("/admin/link-status")
    LinkStatusResponse getlinkAdminStatus();

    @GetMapping("/member/link-status")
    LinkStatusResponse getlinkMemberStatus();
}
