package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomMatchResponse;
import com.nhnacademy.front.core.dto.room.RoomResponse;
import com.nhnacademy.front.core.dto.room.RoomUpdateRequest;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionStatus;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionUpdateRequest;
import com.nhnacademy.front.core.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/rooms")
public class CoreRoomLookupRestController {

    private final RoomService roomService;

    @GetMapping("/by-name")
    public ResponseEntity<List<RoomMatchResponse>> searchRoomsInTeam(
            @PathVariable Long teamId,
            @RequestParam @NotBlank @Size(max = 50) String roomName
    ) {
        return ResponseEntity.ok(roomService.searchRoomsInTeam(teamId, roomName));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoom(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(roomService.getRoom(teamId, roomId));
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long teamId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateRequest request
    ) {
        return ResponseEntity.ok(roomService.updateRoom(teamId, roomId, request));
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

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long teamId,
            @PathVariable Long roomId
    ) {
        roomService.deleteRoom(teamId, roomId);
        return ResponseEntity.noContent().build();
    }
}
