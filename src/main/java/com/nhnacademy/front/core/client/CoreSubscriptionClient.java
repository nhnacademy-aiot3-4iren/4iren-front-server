package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreSubscriptionClient",
        path = "/api/core/teams"
)
public interface CoreSubscriptionClient {

    @PutMapping("/{teamId}/rooms/{roomId}/subscription")
    RoomSubscriptionResponse subscribeToRoom(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/{teamId}/room-subscriptions")
    PageResponse<RoomSubscriptionResponse> getSubscriptions(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @GetMapping("/{teamId}/room-subscriptions/all")
    List<RoomSubscriptionResponse> getAllSubscriptions(
            @PathVariable("teamId") Long teamId
    );

    @PatchMapping("/{teamId}/rooms/{roomId}/subscription")
    RoomSubscriptionResponse updateSubscription(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestBody RoomSubscriptionUpdateRequest request
    );

    @DeleteMapping("/{teamId}/rooms/{roomId}/subscription")
    void unsubscribeFromRoom(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );
}
