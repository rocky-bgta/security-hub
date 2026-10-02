package com.example.contextdemo.dto;

public record ContextResponse(
        String contextId,
        String roomId,
        boolean active) {
}
