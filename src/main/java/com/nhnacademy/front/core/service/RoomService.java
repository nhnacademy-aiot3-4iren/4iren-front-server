package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreRoomClient;
import com.nhnacademy.front.core.client.CoreSubscriptionClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.room.RoomCreateRequest;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.room.RoomResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionStatus;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final CoreRoomClient coreRoomClient;
    private final CoreSubscriptionClient coreSubscriptionClient;

    public PageResponse<RoomDetailResponse> getRooms(
            Long teamId,
            Long buildingId,
            Integer page,
            Integer size,
            String sort
    ) {
        PageResponse<RoomResponse> rooms = coreRoomClient.getRooms(teamId, buildingId, page, size, sort);
        List<RoomDetailResponse> details = rooms.content().stream()
                .map(room -> coreRoomClient.getRoom(teamId, room.roomId()))
                .toList();

        return new PageResponse<>(
                details,
                rooms.page(),
                rooms.size(),
                rooms.totalElements(),
                rooms.totalPages(),
                rooms.first(),
                rooms.last()
        );
    }

    public RoomDetailResponse getRoom(Long teamId, Long roomId) {
        return coreRoomClient.getRoom(teamId, roomId);
    }

    public RoomResponse createRoom(Long teamId, Long buildingId, RoomCreateRequest request) {
        return coreRoomClient.createRoom(teamId, buildingId, request);
    }

    public void deleteRoom(Long teamId, Long roomId) {
        coreRoomClient.deleteRoom(teamId, roomId);
    }

    public RoomSubscriptionStatus getSubscriptionStatus(Long teamId, Long roomId) {
        int page = 0;
        PageResponse<RoomSubscriptionResponse> subscriptions;

        do {
            subscriptions = coreSubscriptionClient.getSubscriptions(teamId, page, 100, "id,ASC");
            for (RoomSubscriptionResponse subscription : subscriptions.content()) {
                if (roomId.equals(subscription.roomId())) {
                    return new RoomSubscriptionStatus(true, subscription.notificationEnabled());
                }
            }
            page++;
        } while (!subscriptions.last());

        return RoomSubscriptionStatus.unsubscribed();
    }

    public Map<Long, RoomSubscriptionStatus> getSubscriptionStatuses(Long teamId, List<Long> roomIds) {
        Map<Long, RoomSubscriptionStatus> statuses = new HashMap<>();
        Set<Long> pendingRoomIds = new HashSet<>(roomIds);

        for (Long roomId : roomIds) {
            statuses.put(roomId, RoomSubscriptionStatus.unsubscribed());
        }

        if (pendingRoomIds.isEmpty()) {
            return statuses;
        }

        int page = 0;
        PageResponse<RoomSubscriptionResponse> subscriptions;

        do {
            subscriptions = coreSubscriptionClient.getSubscriptions(teamId, page, 100, "id,ASC");
            for (RoomSubscriptionResponse subscription : subscriptions.content()) {
                Long roomId = subscription.roomId();
                if (pendingRoomIds.remove(roomId)) {
                    statuses.put(roomId, new RoomSubscriptionStatus(true, subscription.notificationEnabled()));
                }
            }
            page++;
        } while (!subscriptions.last() && !pendingRoomIds.isEmpty());

        return statuses;
    }

    public RoomSubscriptionResponse subscribeToRoom(Long teamId, Long roomId) {
        return coreSubscriptionClient.subscribeToRoom(teamId, roomId);
    }

    public void unsubscribeFromRoom(Long teamId, Long roomId) {
        coreSubscriptionClient.unsubscribeFromRoom(teamId, roomId);
    }

    public RoomSubscriptionResponse updateSubscription(Long teamId, Long roomId, RoomSubscriptionUpdateRequest request) {
        return coreSubscriptionClient.updateSubscription(teamId, roomId, request);
    }
}
