package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.room.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreRoomClient",
        path = "/api/core/teams"
)
public interface CoreRoomClient {

    @PostMapping("/{teamId}/buildings/{buildingId}/rooms")
    RoomResponse createRoom(
            @PathVariable("teamId") Long teamId,
            @PathVariable("buildingId") Long buildingId,
            @RequestBody RoomCreateRequest request
    );

    @GetMapping("/{teamId}/buildings/{buildingId}/rooms")
    PageResponse<RoomResponse> getRooms(
            @PathVariable("teamId") Long teamId,
            @PathVariable("buildingId") Long buildingId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @GetMapping("/{teamId}/rooms/{roomId}")
    RoomDetailResponse getRoom(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/{teamId}/rooms/by-name")
    List<RoomMatchResponse> searchRoomsInTeam(
            @PathVariable("teamId") Long teamId,
            @RequestParam("roomName") String roomName
    );

    @GetMapping("/{teamId}/buildings/{buildingId}/rooms/by-name")
    RoomMatchResponse searchRoomInBuilding(
            @PathVariable("teamId") Long teamId,
            @PathVariable("buildingId") Long buildingId,
            @RequestParam("roomName") String roomName
    );

    @PatchMapping("/{teamId}/rooms/{roomId}")
    RoomResponse updateRoom(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestBody RoomUpdateRequest request
    );

    @DeleteMapping("/{teamId}/rooms/{roomId}")
    void deleteRoom(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
    );
}
