package com.nhnacademy.front.subscription.controller;

import com.nhnacademy.front.subscription.dto.SubscribedRoomResponse;
import com.nhnacademy.front.subscription.service.SubscribedRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubscribedRoomControllerTest {

    @Test
    void opensSubscribedRoomListPage() {
        SubscribedRoomService service = mock(SubscribedRoomService.class);
        List<SubscribedRoomResponse> rooms = List.of(
                new SubscribedRoomResponse(1L, 2L, 3L, "Building", "Room")
        );
        when(service.getSubscribedRooms()).thenReturn(rooms);
        SubscribedRoomController controller = new SubscribedRoomController(service);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.subscribedRoomList(model);

        assertThat(view).isEqualTo("subscription/subscribed-rooms");
        assertThat(model.getAttribute("rooms")).isSameAs(rooms);
    }
}
