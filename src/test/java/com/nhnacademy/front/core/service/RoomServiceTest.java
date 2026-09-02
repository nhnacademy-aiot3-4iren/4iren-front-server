package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreRoomClient;
import com.nhnacademy.front.core.client.CoreSubscriptionClient;
import com.nhnacademy.front.core.dto.subscription.RoomSubscriptionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceTest {

    @Test
    void getsCurrentUsersSubscriptionsFromPublicAllEndpoint() {
        CoreRoomClient roomClient = mock(CoreRoomClient.class);
        CoreSubscriptionClient subscriptionClient = mock(CoreSubscriptionClient.class);
        RoomService service = new RoomService(roomClient, subscriptionClient);
        List<RoomSubscriptionResponse> subscriptions = List.of(
                new RoomSubscriptionResponse(100L, 10L, true)
        );
        when(subscriptionClient.getAllSubscriptions(1L)).thenReturn(subscriptions);

        assertThat(service.getAllSubscriptions(1L)).isSameAs(subscriptions);
        verify(subscriptionClient).getAllSubscriptions(1L);
    }
}
