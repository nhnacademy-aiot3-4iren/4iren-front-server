package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.room.RoomCreateRequest;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionStatus;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionUpdateRequest;
import com.nhnacademy.front.core.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/buildings/{buildingId}/rooms")
public class CoreRoomRestController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<PageResponse<RoomDetailResponse>> getRooms(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(roomService.getRooms(teamId, buildingId, page, size, sort));
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @Valid @RequestBody RoomCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(teamId, buildingId, request));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        roomService.deleteRoom(teamId, roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/subscription")
    public ResponseEntity<RoomSubscriptionStatus> getSubscriptionStatus(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(roomService.getSubscriptionStatus(teamId, roomId));
    }

    @PutMapping("/{roomId}/subscription")
    public ResponseEntity<RoomSubscriptionResponse> subscribeToRoom(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(roomService.subscribeToRoom(teamId, roomId));
    }

    @PatchMapping("/{roomId}/subscription")
    public ResponseEntity<RoomSubscriptionResponse> updateSubscription(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomSubscriptionUpdateRequest request
    ) {
        return ResponseEntity.ok(roomService.updateSubscription(teamId, roomId, request));
    }

    @DeleteMapping("/{roomId}/subscription")
    public ResponseEntity<Void> unsubscribeFromRoom(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        roomService.unsubscribeFromRoom(teamId, roomId);
        return ResponseEntity.noContent().build();
    }
}
