package com.example.contextdemo.controller;

import com.example.contextdemo.dto.ContextIdRequest;
import com.example.contextdemo.dto.MessageResponse;
import com.example.contextdemo.security.JwtPrincipal;
import com.example.contextdemo.service.CurrentContextValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/rooms/a")
public class RoomAController {

    private static final String ROOM_ID = "ROOM-A";
    private final CurrentContextValidator contextValidator;

    public RoomAController(CurrentContextValidator contextValidator) {
        this.contextValidator = contextValidator;
    }

    @PostMapping("/enter")
    public MessageResponse enter(
            @RequestBody ContextIdRequest request,
            Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean valid = contextValidator.isValidContext(
                request.contextId(), principal.userId(), ROOM_ID);
        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have an active Current Context for ROOM-A.");
        }
        return new MessageResponse(
                "Welcome to ROOM-A. JWT and the room-specific Current Context are valid.");
    }
}
