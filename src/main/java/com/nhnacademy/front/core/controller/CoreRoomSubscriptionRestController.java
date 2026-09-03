package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/room-subscriptions")
public class CoreRoomSubscriptionRestController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<PageResponse<RoomSubscriptionResponse>> getSubscriptions(
            @PathVariable Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(roomService.getSubscriptions(teamId, page, size, sort));
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoomSubscriptionResponse>> getAllSubscriptions(@PathVariable Long teamId) {
        return ResponseEntity.ok(roomService.getAllSubscriptions(teamId));
    }
}
