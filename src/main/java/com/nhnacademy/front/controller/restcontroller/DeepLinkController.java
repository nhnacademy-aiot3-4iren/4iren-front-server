package com.nhnacademy.front.controller.restcontroller;

import com.nhnacademy.front.notification.client.NotiDeepLinkClient;
import com.nhnacademy.front.notification.dto.LinkStatusResponse;
import com.nhnacademy.front.notification.dto.LinkTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 텔레그램 딥링크 연동 프록시 컨트롤러.
 * 브라우저 JS가 호출하는 엔드포인트로, 게이트웨이 뒤 Notification 서버로 그대로 전달한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class DeepLinkController {

    private final NotiDeepLinkClient notiDeepLinkClient;

    /**
     * member 봇에 해당 유저가 연동되어있는 지 확인
     * @return LinkStatusResponse.linked (boolean)
     */
    @GetMapping("/member/link-status")
    public ResponseEntity<LinkStatusResponse> getMemberLinkStatus() {
        return ResponseEntity.ok(notiDeepLinkClient.getlinkMemberStatus());
    }

    /**
     * admin 봇에 해당 유저가 연동되어있는 지 확인
     * @return LinkStatusResponse.linked (boolean)
     */
    @GetMapping("/admin/link-status")
    public ResponseEntity<LinkStatusResponse> getAdminLinkStatus() {
        return ResponseEntity.ok(notiDeepLinkClient.getlinkAdminStatus());
    }

    /**
     * member 봇에 유저 연동하기 위한 딥링크 및 제한시간
     * @return LinkTokenResponse
     */
    @PostMapping("/member/link-token")
    public ResponseEntity<LinkTokenResponse> linkMemberToken() {
        return ResponseEntity.ok(notiDeepLinkClient.getlinkMemberToken());
    }

    /**
     * admin 봇에 유저 연동하기 위한 딥링크 및 제한시간
     * @return LinkTokenResponse
     */
    @PostMapping("/admin/link-token")
    public ResponseEntity<LinkTokenResponse> linkAdminToken() {
        return ResponseEntity.ok(notiDeepLinkClient.getlinkAdminToken());
    }
}
