package com.example.contextdemo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "current_contexts")
public class CurrentContext {

    @Id
    private String id;

    private String userId;
    private String jwtId;
    private String roomId;
    private boolean active;
    private Instant createdAt;
    private Instant revokedAt;

    public CurrentContext() {
    }

    public CurrentContext(
            String userId,
            String jwtId,
            String roomId,
            boolean active,
            Instant createdAt) {
        this.userId = userId;
        this.jwtId = jwtId;
        this.roomId = roomId;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getJwtId() {
        return jwtId;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke() {
        this.active = false;
        this.revokedAt = Instant.now();
    }
}
