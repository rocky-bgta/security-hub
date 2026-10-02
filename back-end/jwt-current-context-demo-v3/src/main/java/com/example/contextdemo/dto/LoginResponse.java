package com.example.contextdemo.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds) {
}
