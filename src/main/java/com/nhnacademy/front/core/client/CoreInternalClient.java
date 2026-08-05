package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.subscription.UserRoomSubscriptionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreInternalClient",
        path = "/api/core/internal"
)
public interface CoreInternalClient {

    @GetMapping("/users/{userId}/room-subscriptions")
    UserRoomSubscriptionsResponse getUserSubscriptions(
            @PathVariable("userId") Long userId
    );

    @GetMapping("/teams/{teamId}/users/{userId}/room-subscriptions")
    UserRoomSubscriptionsResponse getUserSubscriptionsInTeam(
            @PathVariable("teamId") Long teamId,
            @PathVariable("userId") Long userId
    );
}
