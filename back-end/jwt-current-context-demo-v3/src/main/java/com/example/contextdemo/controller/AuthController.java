package com.example.contextdemo.controller;

import com.example.contextdemo.dto.LoginRequest;
import com.example.contextdemo.dto.LoginResponse;
import com.example.contextdemo.dto.MessageResponse;
import com.example.contextdemo.security.JwtPrincipal;
import com.example.contextdemo.security.JwtRevocationService;
import com.example.contextdemo.security.JwtService;
import com.example.contextdemo.service.CurrentContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final JwtRevocationService jwtRevocationService;
    private final CurrentContextService currentContextService;

    public AuthController(
            JwtService jwtService,
            JwtRevocationService jwtRevocationService,
            CurrentContextService currentContextService) {
        this.jwtService = jwtService;
        this.jwtRevocationService = jwtRevocationService;
        this.currentContextService = currentContextService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // Demo only: any non-blank username/password is accepted.
        if (request.username() == null
                || request.username().isBlank()
                || request.password() == null
                || request.password().isBlank()) {
            throw new IllegalArgumentException(
                    "username and password are required");
        }

        String token = jwtService.generateToken(request.username());

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds());
    }

    @PostMapping("/logout")
    public MessageResponse logout(
            Authentication authentication,
            HttpServletRequest request) {

        JwtPrincipal principal =
                (JwtPrincipal) authentication.getPrincipal();

        String rawJwt =
                (String) request.getAttribute("rawJwt");

        jwtRevocationService.revoke(
                principal.jwtId(),
                jwtService.getRemainingTtlSeconds(rawJwt));

        currentContextService.revokeAllForJwt(
                principal.jwtId());

        return new MessageResponse(
                "JWT revoked. All active contexts created by this JWT were revoked.");
    }
}
