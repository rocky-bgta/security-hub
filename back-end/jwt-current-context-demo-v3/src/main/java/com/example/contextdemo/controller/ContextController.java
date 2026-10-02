package com.example.contextdemo.controller;

import com.example.contextdemo.dto.ContextIdRequest;
import com.example.contextdemo.dto.ContextResponse;
import com.example.contextdemo.dto.CreateContextRequest;
import com.example.contextdemo.dto.MessageResponse;
import com.example.contextdemo.model.CurrentContext;
import com.example.contextdemo.security.JwtPrincipal;
import com.example.contextdemo.service.CurrentContextService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contexts")
public class ContextController {

    private final CurrentContextService currentContextService;

    public ContextController(CurrentContextService currentContextService) {
        this.currentContextService = currentContextService;
    }

    @PostMapping
    public ContextResponse create(
            @RequestBody CreateContextRequest request,
            Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        CurrentContext context = currentContextService.createContext(
                principal.userId(), principal.jwtId(), request.roomId());
        return new ContextResponse(context.getId(), context.getRoomId(), context.isActive());
    }

    @PostMapping("/revoke")
    public MessageResponse revoke(
            @RequestBody ContextIdRequest request,
            Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        currentContextService.revokeContext(request.contextId(), principal.userId());
        return new MessageResponse("Current Context revoked for contextId=" + request.contextId());
    }
}
