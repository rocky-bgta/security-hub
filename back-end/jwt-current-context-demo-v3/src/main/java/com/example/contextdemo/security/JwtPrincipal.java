package com.example.contextdemo.security;

public record JwtPrincipal(
        String userId,
        String jwtId) {
}
