package com.example.contextdemo.controller;

import com.example.contextdemo.dto.MessageResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/b")
public class RoomBController {

    @GetMapping("/enter")
    public MessageResponse enter() {
        return new MessageResponse(
                "Welcome to Room-B. Only a valid JWT is required.");
    }
}
