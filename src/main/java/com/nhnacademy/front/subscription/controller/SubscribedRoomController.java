package com.nhnacademy.front.subscription.controller;

import com.nhnacademy.front.subscription.service.SubscribedRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SubscribedRoomController {

    private final SubscribedRoomService subscribedRoomService;

    @GetMapping("/rooms/subscriptions")
    public String subscribedRoomList(Model model) {
        model.addAttribute("rooms", subscribedRoomService.getSubscribedRooms());
        return "subscription/subscribed-rooms";
    }
}
